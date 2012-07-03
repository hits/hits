from subprocess import PIPE, Popen

def whatchanged():
    delimiter = "--new-commit--"
    format = delimiter+'%H'
    p = Popen('git whatchanged --format="%s" --name-status'%(format),
            shell=True, stdout=PIPE)
    s = p.stdout.read() # sigh

    text_stream = filter(lambda x: x.strip(), s.split(delimiter))
    return map(parse_commit, text_stream)

def parse_commit(s):
    lines = s.split('\n')
    id = lines[0].strip()

    def parse_line(line):
        words = line.strip().split()
        id = words[0]
        rest = words[1:]
        if not isinstance(rest, str):
            rest = ' '.join(rest)
        return id, rest

    return id, tuple(parse_line(line) for line in lines[1:] if line.strip())

def whatchanged_sql_insert_statements():
    from sql import insert_statement
    insert_commit_actions = lambda x : insert_statement(x, "Commit_actions")
    stream = ({"id":commit, "action":action, "path":path}
                for commit, path_list in whatchanged()
                for action, path in path_list)

    return '\n\n'.join(map(insert_commit_actions, stream))
