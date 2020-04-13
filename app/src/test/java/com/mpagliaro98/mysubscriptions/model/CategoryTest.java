package com.mpagliaro98.mysubscriptions.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the Category class.
 */
public class CategoryTest {

    // The component under test
    private Category CuT;

    /**
     * Run before each test, create the component under test.
     */
    @Before
    public void setup() {
        CuT = new Category(353, "test");
    }

    /**
     * Test getting the color from a category.
     */
    @Test
    public void test_get_color() {
        assertEquals(353, CuT.getColor());
    }

    /**
     * Test getting the name from a category.
     */
    @Test
    public void test_get_name() {
        assertEquals("test", CuT.getName());
    }

    /**
     * Test the string representation of a category is its name.
     */
    @Test
    public void test_to_string() {
        assertEquals("test", CuT.toString());
    }

    /**
     * Test equality between categories of various types.
     */
    @Test
    public void test_equals() {
        Category isEqual = new Category(353, "test");
        Category shouldBeEqual = new Category(353, "TEsT");
        Category notEqualColor = new Category(211, "test");
        Category notEqualName = new Category(353, "name");
        Category notEqual = new Category(163, "cat");
        Integer intNotEqual = 3;
        assertTrue(CuT.equals(isEqual));
        assertTrue(CuT.equals(shouldBeEqual));
        assertFalse(CuT.equals(notEqualColor));
        assertFalse(CuT.equals(notEqualName));
        assertFalse(CuT.equals(notEqual));
        assertFalse(CuT.equals(intNotEqual));
    }
}
