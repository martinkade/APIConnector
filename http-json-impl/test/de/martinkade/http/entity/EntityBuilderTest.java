/*
 * The MIT License
 *
 * Copyright 2016 Martin Kade.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.martinkade.http.entity;

import de.martinkade.http.ApiService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * ...
 * <p/>
 * @author Martin Kade
 * @version Tue, 5 January 2016
 */
public class EntityBuilderTest {

    /**
     * Default constructor.
     */
    public EntityBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testEncode() throws Exception {
        System.out.println("encode");

        final SimpleTestEntity entity = new SimpleTestEntity();
        entity.setLongValue(1L);
        entity.setIntValue(1);
        entity.setFloatValue(1.0f);
        entity.setDoubleValue(1.0d);
        final EntityBuilder<SimpleTestEntity> instance = new EntityBuilder<>();

        final String expResult = "{\"double\":1.0,\"float\":1.0,\"long\":1,\"int\":1}", result = instance.encode(entity);
        assertEquals(expResult, result);
    }

    @Test
    public void testDecode() throws Exception {
        System.out.println("decode");
        EntityBuilder<SimpleTestEntity> instance = new EntityBuilder<>();

        final String json = "{\"long\":1,\"int\":1,\"float\":1.0,\"double\":1.0}";
        final SimpleTestEntity result = instance.decode(json, SimpleTestEntity.class);
        final SimpleTestEntity expResult = new SimpleTestEntity();
        expResult.setLongValue(1L);
        expResult.setIntValue(1);
        expResult.setFloatValue(1.0f);
        expResult.setDoubleValue(1.0d);

        assertEquals(expResult, result);
    }

    @Test
    public void testEncodePrimitiveCollection() throws Exception {
        System.out.println("encode primitive collection");

        final CollectionTestEntity entity = new CollectionTestEntity();
        entity.setLongValues(1L, 2L, 3L);
        final EntityBuilder<CollectionTestEntity> instance = new EntityBuilder<>();

        final String expResult = "{\"longs\":[1,2,3]}", result = instance.encode(entity);
        assertEquals(expResult, result);
    }

    @Test
    public void testDecodePrimitiveCollection() throws Exception {
        System.out.println("decode primitive collection");
        EntityBuilder<CollectionTestEntity> instance = new EntityBuilder<>();

        final String json = "{\"longs\":[1,2,3]}";
        final CollectionTestEntity result = instance.decode(json, CollectionTestEntity.class);
        final CollectionTestEntity expResult = new CollectionTestEntity();
        expResult.setLongValues(1L, 2L, 3L);

        assertEquals(expResult, result);
    }

    /**
     *
     */
    public static class CollectionTestEntity implements ApiService.Entity {

        @JsonAttribute(name = "longs")
        private Set<Long> longValues;

        /**
         * Required default constructor.
         */
        public CollectionTestEntity() {

        }

        public void setLongValues(Long... longValues) {
            this.longValues = new HashSet<>(Arrays.asList(longValues));
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CollectionTestEntity)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.longValues);
            return hash;
        }

    }

    /**
     *
     */
    public static class SimpleTestEntity implements ApiService.Entity {

        @JsonAttribute(name = "long")
        private long longValue;

        @JsonAttribute(name = "int")
        private int intValue;

        @JsonAttribute(name = "float")
        private float floatValue;

        @JsonAttribute(name = "double")
        private double doubleValue;

        /**
         * Required default constructor.
         */
        public SimpleTestEntity() {

        }

        public void setLongValue(long longValue) {
            this.longValue = longValue;
        }

        public void setIntValue(int intValue) {
            this.intValue = intValue;
        }

        public void setDoubleValue(double doubleValue) {
            this.doubleValue = doubleValue;
        }

        public void setFloatValue(float floatValue) {
            this.floatValue = floatValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SimpleTestEntity)) {
                return false;
            }
            return ((SimpleTestEntity) obj).longValue == longValue
                    && ((SimpleTestEntity) obj).intValue == intValue
                    && ((SimpleTestEntity) obj).doubleValue == doubleValue
                    && ((SimpleTestEntity) obj).floatValue == floatValue;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + (int) (this.longValue ^ (this.longValue >>> 32));
            hash = 47 * hash + this.intValue;
            return hash;
        }
    }

}
