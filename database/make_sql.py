import os
import sys

if __name__ == "__main__":
    tablefile = open('./postgresql.tables.sql')
    sys.stdout.write(tablefile.read())

    from parse_log import log_sql_insert_statements
    sys.stdout.write(log_sql_insert_statements())
    from parse_whatchanged import whatchanged_sql_insert_statements
    sys.stdout.write(whatchanged_sql_insert_statements())
