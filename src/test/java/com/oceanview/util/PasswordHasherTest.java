package com.oceanview.util;

import org.junit.Assert;
import org.junit.Test;

public class PasswordHasherTest {

    @Test
    public void hash_shouldReturnSameValue_forSameInput() {
        String h1 = PasswordHasher.hash("test123");
        String h2 = PasswordHasher.hash("test123");

        Assert.assertEquals(h1, h2);
    }

    @Test
    public void hash_shouldReturnDifferentValue_forDifferentInput() {
        String h1 = PasswordHasher.hash("abc");
        String h2 = PasswordHasher.hash("xyz");

        Assert.assertNotEquals(h1, h2);
    }
}
