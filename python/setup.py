#!/usr/bin/env python

from setuptools import setup, Command


class Tester(Command):
    user_options = []

    def initialize_options(self):
        import os
        import os.path
        print("Initializing test")
        self.dbpath = "test/test.db"
        self._dir = os.getcwd()
        if os.path.isfile(self.dbpath):
            import test
            raise test.TestError("Test Database File Exists")

    def finalize_options(self):
        pass

    def run(self):
        import test
        import os
        import os.path
        print("Performing Tests")
        try:
            print("Testing Queue Creation")
            Q = test.create_queue("test", self.dbpath)
            print("PASSED")
            print("Testing Message Addition")
            test.add_messages(Q)
            print("PASSED")
            print("Testing message retrieval")
            test.get_messages(Q)
            print("PASSED")
            print("Cleaning test")
            if os.path.isfile(self.dbpath):
                os.unlink(self.dbpath)
        except test.TestError as te:
            print("Error running test")
            raise te


the_scripts = ['scripts/smq']

setup (name ='smq',
       version = '1.0.b',
       url = 'https://github.com/kd0kfo/smq',
       license = 'GPL v3',
       description = 'SQLite based Message Queue',
       author='David Coss',
       author_email='David.Coss@stjude.org',
       packages = ['smq'],
       scripts = the_scripts,
       ext_package = 'smq',
       cmdclass={'test': Tester})

