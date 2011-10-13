/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.wisc.nexus.auth.rut.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * Loads/saves/refreshes data from a file in a thread-safe manner
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @param <C> Data type file is loaded into
 */
public abstract class AbstractRefreshingFileLoader<C> extends AbstractLogEnabled implements Initializable, Disposable {

    //Configuration should only ever be touched within the configLock
    private final ReadWriteLock configReadWriteLock = new ReentrantReadWriteLock();
    private final Timer configurationSaveTimer;

    private TimerTask configurationRefresh;
    private long lastModified = Long.MIN_VALUE;
    private long lastSize = Long.MIN_VALUE;
    private C configuration;

    /**
     * @param timerName Name of the timer to create
     */
    public AbstractRefreshingFileLoader(String timerName) {
        final String timerFullName = timerName + ".RefreshTimer";
        this.configurationSaveTimer = new Timer(timerFullName, true);
    }

    /**
     * @return The configuration file to read from
     */
    protected abstract File getConfigurationFile();

    /**
     * @return The frequency in seconds that the configuration file should be refreshed.
     */
    protected abstract int getRefreshInterval(C configuration);

    /**
     * Save the configuration file using the config specific serializer, must be implemented if {@link #preSave(Object)} ever
     * returns true;
     */
    @SuppressWarnings("unused")
    protected void writeConfiguration(Writer w, C configuration) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Read the configuration file using the config specific deserializer
     */
    protected abstract C readConfiguration(Reader r) throws IOException;

    /**
     * Called from {@link #initialize()} after the first time the configuration is loaded
     */
    @SuppressWarnings("unused")
    protected void initializeInternal() throws InitializationException {
    }

    /**
     * Called from {@link #dispose()} before the last call to {@link #refreshConfiguration()};
     */
    protected void disposeInternal() {
    }

    /**
     * Called by {@link #readConfiguration(InputStream)} after the latest config is loaded from the filesystem.
     * Allows for modification of the configuration which is then written back out. Called within a mutex specific
     * to loading of the config file.
     * 
     * @return If true the configuration will be saved via {@link #writeConfiguration(OutputStream, Object)}
     */
    protected boolean preSave(C configuration) {
        return false;
    }

    /**
     * Called after the configuration is loaded from the filesystem. Called within a mutex specific to loading of the
     * config file.
     */
    protected void postLoad(C configuration) {
    }
    
    /**
     * @return Read Lock that can be used by subclasses for operations that need to be exclusive from the config refresh  
     */
    protected Lock getReadLock() {
        return this.configReadWriteLock.readLock();
    }
    

    /**
     * Initializes the configuration and calls {@link #initializeInternal()}. Multiple calls to this
     * method will only result in the load & {@link #initializeInternal()} being called once so it is
     * safe for subclasses to use this to ensure initialization
     */
    @Override
    public final void initialize() throws InitializationException {
        this.configReadWriteLock.readLock().lock();
        try {
            if (this.configuration != null) {
                return;
            }
        }
        finally {
            this.configReadWriteLock.readLock().unlock();
        }
        
        this.configReadWriteLock.writeLock().lock();
        try {
            if (this.configuration != null) {
                return;
            }
            this.loadConfiguration();
            this.initializeInternal();
        }
        finally {
            this.configReadWriteLock.writeLock().unlock();
        }
    }

    /* (non-Javadoc)
      * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable#dispose()
      */
    @Override
    public final void dispose() {
        this.configurationSaveTimer.cancel();
        this.configurationSaveTimer.purge();
        this.disposeInternal();
        this.refreshConfiguration(false);
    }
    
    /**
     * Refresh the configuration file, optionally saving modifications back to the file system
     */
    protected final void refreshConfiguration() {
        this.refreshConfiguration(true);
    }
    
    private void refreshConfiguration(boolean doLoad) {
        this.configReadWriteLock.writeLock().lock();
        try {
            final C configuration = getConfiguration();

            final boolean save = this.preSave(configuration);
            if (save) {
                saveConfiguration(configuration);
            }
            if (doLoad) {
                this.loadConfiguration(configuration);
            }
        }
        finally {
            this.configReadWriteLock.writeLock().unlock();
        }
    }

    
    private final void loadConfiguration() {
        this.configReadWriteLock.writeLock().lock();
        try {
            final C configuration = getConfiguration();
            this.loadConfiguration(configuration);
        }
        finally {
            this.configReadWriteLock.writeLock().unlock();
        }
    }

    private final void loadConfiguration(C configuration) {
        this.configReadWriteLock.writeLock().lock();
        try {
            //Cancel the refresh timer if it exists
            if (configurationRefresh != null) {
                configurationRefresh.cancel();
                configurationRefresh = null;
                this.configurationSaveTimer.purge();
            }

            this.postLoad(configuration);

            final int saveInterval = getRefreshInterval(configuration);
            if (saveInterval > 0) {
                //Create a new task to periodically save the user cache
                this.configurationRefresh = new TimerTask() {
                    @Override
                    public void run() {
                        refreshConfiguration();
                    }
                };

                final long period = TimeUnit.SECONDS.toMillis(saveInterval);
                this.configurationSaveTimer.schedule(this.configurationRefresh, period, period);
            }
        }
        finally {
            this.configReadWriteLock.writeLock().unlock();
        }
    }

    private C getConfiguration() {
        final Logger logger = this.getLogger();

        final File configurationFile = this.getConfigurationFile();
        
        final boolean shouldLoadConfiguration = shouldLoadConfiguration(configurationFile);
        if (!shouldLoadConfiguration) {
            logger.debug("File has not been modified since last load, returning cached load: " + configurationFile);
            return this.configuration;
        }
        
        Reader r = null;
        try {
            r = new FileReader(configurationFile);
            this.lastModified = configurationFile.lastModified();
            this.lastSize = configurationFile.length();
            this.configuration = this.readConfiguration(r);
            return this.configuration;
        }
        catch (final FileNotFoundException e) {
            logger.warn("File does not exist: " + configurationFile.getAbsolutePath() + ", returning null", e);
        }
        catch (final IOException e) {
            logger.warn("IOException while retrieving file: " + configurationFile.getAbsolutePath() + " returning null.", e);
        }
        finally {
            IOUtils.closeQuietly(r);
        }

        return null;
    }
    
    private boolean shouldLoadConfiguration(File configurationFile) {
        final Logger logger = this.getLogger();
        
        if (this.configuration == null) {
            logger.info("First load of: " + configurationFile.getAbsolutePath());
            return true;
        }
        
        if (this.lastModified < configurationFile.lastModified() || this.lastSize != configurationFile.length()) {
            logger.info("File has been modified: " + configurationFile.getAbsolutePath());
            return true;
        }
        
        return false;
    }
    
    private void saveConfiguration(final C configuration) {
        final Logger logger = this.getLogger();
        
        final File configurationFile = this.getConfigurationFile();
        Writer w = null;
        try {
            w = new FileWriter(configurationFile);
            logger.info("Saving configuration file: " + configurationFile.getAbsolutePath());
            this.writeConfiguration(w, configuration);
        }
        catch (final FileNotFoundException e) {
            logger.warn("Configuration file does not exist: " + configurationFile.getAbsolutePath() + ", nothing will be saved", e);
            return;
        }
        catch (final IOException e) {
            logger.warn("IOException while saving file: " + configurationFile.getAbsolutePath() + ", nothing will be saved", e);
            return;
        }
        finally {
            IOUtils.closeQuietly(w);
        }

        this.lastModified = configurationFile.lastModified();
        this.lastSize = configurationFile.length();
    }
}
