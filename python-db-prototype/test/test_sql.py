from database.sql import *

def test_mysql_stringify():
    assert mysql_stringify("hello") == '"hello"'
def test_postgres_stringify():
    assert postgres_stringify("hello") == "'hello'"

def test_insert_statement_mysql():
    assert insert_statement({'name': 'joe'}, "NameTable",
                            mysql_sanitize, mysql_stringify ) == \
            """INSERT INTO NameTable\n(name)\nVALUES\n("joe");"""
    assert insert_statement({'name': 'joe', 'age': '14'}, "NameTable",
                            mysql_sanitize, mysql_stringify ) == \
            """INSERT INTO NameTable\n(age, name)\nVALUES\n("14", "joe");"""

def test_insert_many_statement_mysql():
    data = ({'name': 'joe',   'age': '14'}, {'name': 'alice', 'age': '22'})
    assert insert_many_statement(data, "NameTable",
                            mysql_sanitize, mysql_stringify ) == \
            """INSERT INTO NameTable\n(age, name)\nVALUES\n("14", "joe"),\n("22", "alice");"""

def test_insert_statement_postgres():
    assert insert_statement({'name': 'joe'}, "NameTable") == \
            """INSERT INTO NameTable\n(name)\nVALUES\n('joe');"""
    assert insert_statement({'name': 'joe', 'age': '14'}, "NameTable") == \
            """INSERT INTO NameTable\n(age, name)\nVALUES\n('14', 'joe');"""

def test_insert_many_statement_postgres():
    data = ({'name': 'joe',   'age': '14'}, {'name': 'alice', 'age': '22'})
    assert insert_many_statement(data, "NameTable") == \
            """INSERT INTO NameTable\n(age, name)\nVALUES\n('14', 'joe'),\n('22', 'alice');"""
