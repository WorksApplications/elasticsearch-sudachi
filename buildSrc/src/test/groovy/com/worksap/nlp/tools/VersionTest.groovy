package com.worksap.nlp.tools

import org.junit.Assert
import org.junit.Test

class VersionTest {
    @Test
    void ge() {
        var v = new Version(3, 4, "3.4")
        Assert.assertTrue(v.ge(2, 10))
        Assert.assertTrue(v.ge(3, 3))
        Assert.assertTrue(v.ge(3, 4))
        Assert.assertFalse(v.ge(3, 5))
        Assert.assertFalse(v.ge(4, 1))
        Assert.assertFalse(v.ge(4, 5))
    }

    @Test
    void lt() {
        var v = new Version(3, 4, "3.4")
        Assert.assertFalse(v.lt(2, 10))
        Assert.assertFalse(v.lt(3, 3))
        Assert.assertFalse(v.lt(3, 4))
        Assert.assertTrue(v.lt(3, 5))
        Assert.assertTrue(v.lt(3, 10))
        Assert.assertTrue(v.lt(4, 10))
    }
}
