import re

git_encoding = 'latin-1'

def mysql_stringify(s):
    return '"%s"'%s
def postgres_stringify(s):
    return "'%s'"%s

bad_chars = {'"', "'"}
def mysql_sanitize(s):
    for char in bad_chars:
        s = s.replace(char, '\\'+char)
    return s

def postgres_sanitize(s):
    return s.replace("'", "''")

def chain(*fns):
    def fn(x):
        for subfn in fns:
            x = subfn(x)
        return x
    return fn

def parenthesize(s):
    return "(%s)"%s

def decode(s):
    return s.decode(git_encoding)

def insert_statement(d, table_name, sanitize  = postgres_sanitize,
                                    stringify = postgres_stringify):
    keys = sorted(d.keys())
    values = [d[key] for key in keys]


    return "INSERT INTO %s\n(%s)\nVALUES\n(%s);"%(
            table_name,
            ', '.join(keys),
            ', '.join(map(chain(sanitize, stringify), values)))

def insert_many_statement(ds, table_name, sanitize  = postgres_sanitize,
                                          stringify = postgres_stringify):
    keys = sorted(ds[0].keys())
    values = ',\n'.join(
        [parenthesize(', '.join(map(chain(sanitize, stringify, decode),
                                [d[key] for key in keys])))
                      for d in ds])

    return "INSERT INTO %s\n(%s)\nVALUES\n%s;"%(
            table_name,
            ', '.join(keys),
            values)
