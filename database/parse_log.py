from subprocess import PIPE, Popen
# http://blog.lost-theory.org/post/how-to-parse-git-log-output/

git_fields = {'id': '%H',
              'author_name': '%an',
              'author_email':'%ae',
              'date': '%ad',
              'timestamp': '%at',
              'subject': '%s',
              'body': '%B'}

fields, formats= zip(*git_fields.items())

format = '%x1f'.join(formats) + '%x1e'

def log():
    p = Popen('git log --format="%s"' % format, shell=True, stdout=PIPE)
    s = p.stdout.read() # sigh
    text_stream = s.split('\x1e')
    split_stream = (row.strip().split("\x1f") for row in text_stream)
    dict_stream = (dict(zip(fields, row)) for row in split_stream
            if len(row)==len(fields))
    return dict_stream

def log_sql_insert_statements():
    from sql import insert_statement, insert_many_statement
    return insert_many_statement(list(log()), "Commit_log")
    # insert_commit_log = lambda x : insert_statement(x, "Commit_log")
    # return '\n\n'.join(map(insert_commit_log, log()))
