

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
    print("How many total messages are there? %d" % Q.count_messages())
    print("Does Charlie have any messages?")
    if Q.has_message("Charlie"):
        print("Did not expect Charlie to have a message. ERROR!")
    else:
        print("No, that's good. We didn't send him a message.")
    print("Getting Bob's Message")
    print("Does Bob have a message?")
    if Q.has_message("Bob"):
        print("Yes, %d message(s)" % Q.count_messages("Bob"))
    else:
        raise TestError("Error calling has_messages")
    msg = Q.next("Bob")
    print("Bob received a %s (%s) message: %s" % (msg.content_type,
                                                  type(msg.content),
                                                  msg.content))
    print("Getting Alice's %d Messages using a while loop" %
          Q.count_messages("Alice"))

    while Q.has_message("Alice"):
        msg = Q.next("Alice")
        print("Alice received a %s (%s) message: %s" %
              (msg.content_type, type(msg.content), msg.content))
