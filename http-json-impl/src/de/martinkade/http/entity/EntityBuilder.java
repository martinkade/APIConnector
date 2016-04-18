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

import de.martinkade.http.ApiException;
import de.martinkade.http.ApiService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * ...
 * <p/>
 *
 * @param <T>
 * @author Martin Kade
 * @version Tue, 5 January 2016
 */
public class EntityBuilder<T extends ApiService.Entity> {

    /**
     * Current entity class.
     */
    private Class<T> entityClass;

    /**
     * Default constructor.
     */
    public EntityBuilder() {

    }

    /**
     * @param entity
     * @return
     * @throws ApiException
     */
    public String encode(T entity) throws ApiException {
        entityClass = (Class<T>) entity.getClass();
        final Field[] fields = entityClass.getDeclaredFields();

        return buildJsonObject(entity, fields).toJSONString();
    }

    /**
     * @param entity
     * @param fields
     * @return
     * @throws ApiException
     */
    private JSONObject buildJsonObject(T entity, Field[] fields)
            throws ApiException {

        final JSONObject json = new JSONObject();
        for (Field f : fields) {
            final JsonAttribute attr = getAttribute(f);
            if (attr != null) {
                setJsonAttrValue(f, entity, json, attr.name());
            }
        }
        return json;
    }

    /**
     * @param field
     * @param entity
     * @param json
     * @param attrName
     * @throws ApiException
     */
    private void setJsonAttrValue(Field field, T entity, JSONObject json,
            String attrName) throws ApiException {

        field.setAccessible(true);
        try {
            Object value = field.get(entity);

            if (value instanceof ApiService.Entity) {
                final Class<T> tmpClass = (Class<T>) value.getClass();
                value = buildJsonObject((T) value,
                        tmpClass.getDeclaredFields());
            } else if (value instanceof Collection) {
                final Class<?> tmpClass = (Class<?>) ((ParameterizedType) field
                        .getGenericType()).getActualTypeArguments()[0];
                final JSONArray jsonArray = new JSONArray();
                for (Object o : (Collection) value) {
                    if (ApiService.Entity.class.isAssignableFrom(tmpClass)) {
                        jsonArray.add(buildJsonObject((T) o,
                                tmpClass.getDeclaredFields()));
                    } else if (isPrimitive(tmpClass)) {
                        jsonArray.add(o);
                    }
                }
                value = jsonArray.isEmpty() ? null : jsonArray;
            } else if (value instanceof LocalDate) {
                value = ((LocalDate) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else if (value instanceof LocalTime) {
                value = ((LocalTime) value).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            } else if (value instanceof LocalDateTime) {
                value = ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else if (value instanceof Enum) {
                value = ((Enum) value).name().toLowerCase();
            }

            json.put(attrName, value);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new ApiException(ex.getMessage(),
                    ApiException.APIError.CONNECTION_TIMEOUT);
        }
    }

    /**
     * @param jsonString
     * @param entityClass
     * @return
     * @throws ParseException
     * @throws ApiException
     */
    public T decode(String jsonString, Class<T> entityClass)
            throws ApiException, ParseException {

        this.entityClass = entityClass;

        if (entityClass == null) {
            return null;
        }

        final JSONParser parser = new JSONParser();
        final JSONObject json = (JSONObject) parser.parse(jsonString);

        final Field[] fields = requestFields(entityClass);

        return buildObject(json, instantiateEntity(entityClass), fields);
    }

    /**
     *
     * @param clazz
     * @return
     */
    private Field[] requestFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz.getSuperclass() != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields.toArray(new Field[fields.size()]);
    }

    /**
     * @param json
     * @param entity
     * @param fields
     * @throws ApiException
     */
    private T buildObject(JSONObject json, T entity, Field[] fields)
            throws ApiException {

        for (Field f : fields) {
            final JsonAttribute attr = getAttribute(f);
            if (attr != null && json.containsKey(attr.name())) {
                setFieldValue(f, entity, json.get(attr.name()));
            }
        }
        return entity;
    }

    /**
     * @param field
     * @param entity
     * @param value
     * @throws ApiException
     */
    private void setFieldValue(Field field, T entity, Object value) throws ApiException {

        Object v;
        field.setAccessible(true);
        final Class<?> fieldType = field.getType();
        try {

            if (ApiService.Entity.class.isAssignableFrom(fieldType)
                    && (value instanceof JSONObject)) {

                final Class<T> tmpClass = (Class<T>) fieldType;
                v = buildObject((JSONObject) value,
                        instantiateEntity(tmpClass),
                        tmpClass.getDeclaredFields());
            } else if (Collection.class.isAssignableFrom(fieldType)
                    && (value instanceof JSONArray)) {

                final Class<?> tmpClass = (Class<?>) ((ParameterizedType) field
                        .getGenericType()).getActualTypeArguments()[0];

                Collection array;
                if (fieldType.isInterface() || Modifier.isAbstract(fieldType.getModifiers())) {
                    if (Set.class.isAssignableFrom(fieldType)) {
                        array = new HashSet();
                    } else if (List.class.isAssignableFrom(fieldType)) {
                        array = new ArrayList();
                    } else {
                        throw new ApiException("collection type is not compatible",
                                ApiException.APIError.CONNECTION_TIMEOUT);
                    }
                } else {
                    Constructor<?> c = (Constructor<?>) fieldType.getConstructor();
                    array = (Collection) c.newInstance();
                    if (array == null) {
                        throw new ApiException("cannot instantiate collection type",
                                ApiException.APIError.CONNECTION_TIMEOUT);
                    }
                }

                for (Object o : (JSONArray) value) {
                    if (ApiService.Entity.class.isAssignableFrom(tmpClass)) {
                        array.add(buildObject((JSONObject) o,
                                instantiateEntity((Class<T>) tmpClass),
                                tmpClass.getDeclaredFields()));
                    } else if (isPrimitive(tmpClass)) {
                        array.add(o);
                    }
                }
                v = array;
            } else if (LocalDate.class.isAssignableFrom(fieldType)) {
                v = LocalDate.parse((String) value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else if (LocalTime.class.isAssignableFrom(fieldType)) {
                v = LocalTime.parse((String) value, DateTimeFormatter.ofPattern("HH:mm:ss"));
            } else if (LocalDateTime.class.isAssignableFrom(fieldType)) {
                v = LocalDateTime.parse((String) value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else if (Integer.class.isAssignableFrom(fieldType) || fieldType.getName().equals("int")) {
                v = ((Number) value).intValue();
            } else if (Float.class.isAssignableFrom(fieldType) || fieldType.getName().equals("float")) {
                v = ((Number) value).floatValue();
            } else if (Enum.class.isAssignableFrom(fieldType)) {
                v = Enum.valueOf((Class<? extends Enum>) fieldType, ((String) value).toUpperCase());
            } else {
                v = (value instanceof JSONObject) ? null : value;
            }

            field.set(entity, v);
        } catch (InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException | IllegalArgumentException | IllegalAccessException ex) {
            throw new ApiException(ex.getMessage(),
                    ApiException.APIError.CONNECTION_TIMEOUT);
        }
    }

    /**
     * @param field
     * @return
     */
    private JsonAttribute getAttribute(Field field) {
        if (field.isAnnotationPresent(JsonAttribute.class)) {
            return field.getAnnotation(JsonAttribute.class);
        }
        return null;
    }

    /**
     * @param entityClass
     * @return
     * @throws ApiException
     */
    private T instantiateEntity(Class<T> entityClass) throws ApiException {
        try {
            final Constructor<T> constructor = entityClass.getConstructor();
            final T entity = constructor.newInstance();
            return entity;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            throw new ApiException(ex.getMessage(),
                    ApiException.APIError.CONNECTION_TIMEOUT);
        }
    }

    /**
     *
     * @param clazz
     * @return
     */
    private boolean isPrimitive(Class clazz) {
        if (clazz.isPrimitive()) {
            return true;
        } else {
            return Integer.class.isAssignableFrom(clazz)
                    || Long.class.isAssignableFrom(clazz)
                    || Float.class.isAssignableFrom(clazz)
                    || Double.class.isAssignableFrom(clazz)
                    || Boolean.class.isAssignableFrom(clazz);
        }
    }
}
