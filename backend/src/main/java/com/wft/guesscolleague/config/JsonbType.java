package com.wft.guesscolleague.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * Пользовательский тип Hibernate для работы с JSONB полями в PostgreSQL
 * Позволяет сохранять Java объекты как JSON в базе данных
 *
 * Используется в QuestionAttempt для поля "options"
 */
public class JsonbType implements UserType<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * SQL тип для JSONB в PostgreSQL
     */
    @Override
    public int getSqlType() {
        return Types.OTHER;  // OTHER соответствует JSONB в PostgreSQL
    }

    /**
     * Класс Java, который представляет этот тип
     */
    @Override
    public Class<Object> returnedClass() {
        return Object.class;
    }

    /**
     * Сравнение двух значений
     */
    @Override
    public boolean equals(Object x, Object y) {
        return Objects.equals(x, y);
    }

    /**
     * Хэш-код значения
     */
    @Override
    public int hashCode(Object x) {
        return Objects.hashCode(x);
    }

    /**
     * Чтение JSON из ResultSet и преобразование в Java объект
     * Вызывается Hibernate при загрузке данных из БД
     */
    @Override
    public Object nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        String json = rs.getString(position);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to parse JSON", e);
        }
    }

    /**
     * Запись Java объекта в JSON и сохранение в БД
     * Вызывается Hibernate при сохранении данных
     */
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            try {
                String json = objectMapper.writeValueAsString(value);
                st.setObject(index, json, Types.OTHER);
            } catch (JsonProcessingException e) {
                throw new SerializationException("Failed to serialize to JSON", e);
            }
        }
    }

    /**
     * Глубокое копирование значения
     */
    @Override
    public Object deepCopy(Object value) {
        if (value == null) return null;
        try {
            String json = objectMapper.writeValueAsString(value);
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to deep copy JSON", e);
        }
    }

    /**
     * Является ли тип изменяемым
     */
    @Override
    public boolean isMutable() {
        return true;
    }

    /**
     * Разбор значения для кэширования
     */
    @Override
    public Serializable disassemble(Object value) {
        return (Serializable) deepCopy(value);
    }

    /**
     * Сборка значения из кэша
     */
    @Override
    public Object assemble(Serializable cached, Object owner) {
        return deepCopy(cached);
    }
}