"""
SMQ
===

Simple Message Queue using SQLite storage.

"""

CREATE_QUEUE_SQL = ("create table if not exists queue (id INTEGER PRIMARY KEY,"
                    " name STRING UNIQUE NOT NULL, description STRING);")
CREATE_MESSAGE_QUEUE_SQL = ("create table if not exists messages "
                            "(id INTEGER PRIMARY KEY, queueid INTEGER, "
                            "message STRING NOT NULL, message_type STRING, "
                            "sender STRING, target STRING, "
                            "FOREIGN KEY(queueid) REFERENCES queue(id));")


def get_version():
    import pkg_resources
    try:
        dist = pkg_resources.get_distribution("dag")
        if dist:
            return dist.version
    except pkg_resources.DistributionNotFound:
        pass
    return "0.0"


__version__ = get_version()


def open_db(dbpath):
    import sqlite3
    return sqlite3.connect(dbpath)


class DBError(Exception):
    pass


class Message():
    def __init__(self, content, content_type, sender, recipient,
                 messageid=None, queueid=None):
        self.content = content
        self.content_type = content_type
        self.sender = sender
        self.recipient = recipient
        self.queueid = queueid
        self.id = messageid

    @staticmethod
    def from_sql_row(row):
        import sqlite3
        if not row:
            return None
        return Message(row['message'], row['message_type'], row['sender'],
                        row['target'], row['id'], row['queueid'])


class Queue():
    def __init__(self, name, dbpath=None):
        import sqlite3
        self.name = name
        self.queueid = None
        if not dbpath:
            self.dbpath = "%s.db" % name
        else:
            self.dbpath = dbpath
        db = None
        try:
            db = open_db(self.dbpath)
            db.row_factory = sqlite3.Row
            c = db.cursor()
            for sql in (CREATE_QUEUE_SQL, CREATE_MESSAGE_QUEUE_SQL):
                c.execute(sql)
            db.commit()
            c.execute("insert or ignore into queue values (NULL, ?, NULL);",
                      (name,))
            db.commit()
            c.execute("select id from queue where name = ?;", (name,))
            row = c.fetchone()
            if not row:
                raise DBError("Could not get id for queue '%s' using database "
                              "'%s'" % (self.name, self.dbpath))
            self.queueid = int(row['id'])
        finally:
            if c:
                c.close()

    def send(self, message):
        db = None
        try:
            db = open_db(self.dbpath)
            c = db.cursor()
            c.execute("insert into messages values "
                      "(NULL, ?, ?, ?, ?, ?);",
                      (self.queueid, message.content, message.content_type,
                       message.sender, message.recipient))
            db.commit()
        finally:
            if db:
                db.close()

    def peek(self, recipient=None):
        db = None
        try:
            import sqlite3
            db = open_db(self.dbpath)
            db.row_factory = sqlite3.Row
            c = db.cursor()
            if recipient:
                c.execute("select * from messages where queueid == ? and "
                          "target == ? order by id asc limit 1;",
                          (self.queueid, recipient))
            else:
                c.execute("select * from messages where queueid == ? "
                          "order by id asc limit 1;", (self.queueid,))
            return Message.from_sql_row(c.fetchone())
        finally:
            if db:
                db.close()

    def next(self, recipient=None):
        message = self.peek(recipient)
        if not message:
            return None
        db = None
        try:
            db = open_db(self.dbpath)
            c = db.cursor()
            c.execute("delete from messages where id == ?;", (message.id,))
            db.commit()
        finally:
            if db:
                db.close()
        return message

    def has_message(self, recipient=None):
        db = None
        try:
            import sqlite3
            db = open_db(self.dbpath)
            db.row_factory = sqlite3.Row
            c = db.cursor()
            if recipient:
                c.execute("select count(*) from messages where queueid == ? and "
                          "target == ? order by id asc limit 1;",
                          (self.queueid, recipient))
            else:
                c.execute("select count(*) from messages where queueid == ? "
                          "order by id asc limit 1;", (self.queueid,))
            row = c.fetchone()
            return row[0]
        finally:
            if db:
                db.close()

