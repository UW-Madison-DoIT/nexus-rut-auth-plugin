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

package edu.wisc.nexus.auth.rut.dao;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MD5CryptTest {
    @Test
    public void testKnownSaltApacheCrypt() {
        final String encrypted = MD5Crypt.apacheCrypt("RJDgUmkY6YtCGf5a", "/njf5ImR");
        assertEquals("$apr1$/njf5ImR$wfW0I6J6SIiw0PtuWdwPk1", encrypted);
        
        assertTrue(MD5Crypt.verifyPassword("RJDgUmkY6YtCGf5a", encrypted));
    }
    
    @Test
    public void testUnknownSaltApacheCrypt() {
        final String encrypted = MD5Crypt.apacheCrypt("RJDgUmkY6YtCGf5a");
        
        assertTrue(MD5Crypt.verifyPassword("RJDgUmkY6YtCGf5a", encrypted));
    }
}
