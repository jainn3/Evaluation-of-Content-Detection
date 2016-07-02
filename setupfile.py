import os
from setuptools import setup

def read(fname):
    return open(os.path.join(os.path.dirname(__file__), fname)).read()

setup(
    name = "measurements_part10",
    version = "0.1",
    author = "Team 18, CSCI 599",
    keywords = "ner,measurements,nltk,range",
    use_2to3=True,
    long_description=read('README.md'),
    install_requires=['nltk','tika'],
)