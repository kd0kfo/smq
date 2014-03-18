

class TestError(Exception):
    pass


def create_queue(name, path):
    import smq
    return smq.Queue(name, path)


def add_messages(Q):
    import smq
    msg = smq.Message("This is a test string.", "string", "Tester", "Alice")
    Q.send(msg)
    msg = smq.Message(42, "int", "Tester", "Bob")
    Q.send(msg)
    msg = smq.Message(3.14, "float", "Bob", "Alice")
    Q.send(msg)
    print("Sent three messages. One to Bob and two to Alice")

def get_messages(Q):
    print("Getting Bob's Message")
    print("Does Bob have a message?")
    if Q.has_message("Bob"):
        print("Yes")
    else:
        raise TestError("Error calling has_messages")
    msg = Q.next("Bob")
    print("Bob received a %s (%s) message: %s" % (msg.content_type,
                                                  type(msg.content),
                                                  msg.content))
    print("Getting Alice's Messages using a while loop")
    
    while Q.has_message("Alice"):
        msg = Q.next("Alice")
        print("Alice received a %s (%s) message: %s" %
              (msg.content_type, type(msg.content), msg.content)) 
