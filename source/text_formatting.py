class OutputFormatter:

    def __init__(self, fileName):
        self.fileStream = open(fileName, 'w+')
        self.indentation = 0

    def addIndentation(self):
        self.indentation += 1

    def removeIndentation(self):
        self.indentation -= 1 if self.indentation > 0 else self.indentation

    def getIndentationString(self):
        string = ""
        for it in range(0, self.indentation):
            string += '\t'
        return string

    def writeLine(self, string, escapeChar = '\n', applyIndentation = True, forceCurly = False):
        line = self.getIndentationString() if applyIndentation else ''
        if forceCurly:
            line += string + escapeChar
        else:
            line += self.toGroovy(string) + escapeChar

        self.fileStream.write(line)

    def toGroovy(self, string):
        string = string.replace('{', '[')
        string = string.replace('}', ']')
        return string