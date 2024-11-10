package com.example.petranationwidemovies.dao;

import java.sql.SQLException;

public interface Dao { // abstaction, parameter object biar custom, bisa dimasukin bnyk hal
    int add(Object element) throws SQLException; // sqlexception gunannya untuk kalau ada error waktu execute querry biar bisa ditangkep sm trycatch (error handling)
    Object get() throws SQLException;
    int update(Object element) throws SQLException;
    Object delete(int id) throws SQLException;
}
