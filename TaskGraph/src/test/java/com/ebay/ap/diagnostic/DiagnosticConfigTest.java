/*
 * Copyright 2022 eBay Inc.
 *  Author/Developer: Damian Dolan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ebay.ap.diagnostic;

import org.junit.Assert;
import org.junit.Test;

public class DiagnosticConfigTest {

    @Test
    public void testProfilePercentage() {
        
        boolean profile = DiagnosticConfig.doProfile(.0d, 0);
        Assert.assertFalse(profile);

        profile = DiagnosticConfig.doProfile(.01d, 0);
        Assert.assertTrue(profile);

        for (int count = 1; count < 100; ++count) {
            profile = DiagnosticConfig.doProfile(.01d, count);
            Assert.assertFalse(profile);
        }
        profile = DiagnosticConfig.doProfile(.1d, 100);

        Assert.assertTrue(profile);
        for (int count = 101; count < 110; ++count) {
            profile = DiagnosticConfig.doProfile(.1d, count);
            Assert.assertFalse(profile);
        }
        profile = DiagnosticConfig.doProfile(.1d, 110);
        Assert.assertTrue(profile);
    }
}
