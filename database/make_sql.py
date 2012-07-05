import os
import sys
import codecs

encoding = 'utf-8'

if __name__ == "__main__":

    if len(sys.argv) != 2:
        print "Usage:\n%s outfile.txt"%sys.argv[0]
        sys.exit(-1)

    outfile = codecs.open(sys.argv[1], 'w', encoding)

    def write(s):
        #sys.stdout.write(s)
        outfile.write(s)

    tablefile = open('./postgresql.tables.sql')
    write(tablefile.read())

    from parse_log import log_sql_insert_statements
    write(log_sql_insert_statements())
    from parse_whatchanged import whatchanged_sql_insert_statements
    write(whatchanged_sql_insert_statements())
