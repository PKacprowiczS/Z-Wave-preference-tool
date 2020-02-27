import argparse
import xml.etree.ElementTree as element_tree
import json
from text_formatting import OutputFormatter

def createParametersList(parameterRoot):
    parametersList = list()
    for name in parameterRoot:
        parameterDictionary = dict()
        isBooleanCandidate = False

        parameterDictionary['name'] = name.find('Name').text
        parameterDictionary['key'] = name.find('Name').text.title().replace(' ','')
        parameterDictionary['key'] = parameterDictionary['key'][0].lower() + parameterDictionary['key'][1:]
        parameterDictionary['description'] = name.find('Description').text.replace('\n',' ')
        parameterDictionary['parameterNumber'] = name.find('ParameterNumber').text
        parameterDictionary['size'] = name.find('Size').text
        parameterDictionary['defaultValue'] = name.find('DefaultValue').text

        parameterValues = name.find('ConfigurationParameterValues')
        numberOfParameterValues = len(parameterValues)
        enumableValuesCounter = 0
        values = dict()
        isRangeWithBooleanCandidate = False
        isOnlyRangeCandidate = False
        for value in parameterValues:
            valFrom = int(value.find('From').text)
            valTo = int(value.find('To').text)
            valTo = valTo if valTo > valFrom else valFrom
            isOnlyRangeCandidate = valFrom <= int(parameterDictionary['defaultValue']) <= valTo or isOnlyRangeCandidate
            isBooleanCandidate = (valFrom == valTo)
            enumableValuesCounter = enumableValuesCounter + 1 if isBooleanCandidate else enumableValuesCounter
            key = valFrom if isBooleanCandidate else str(valFrom) + '..' + str(valTo)
            values[str(key)] = value.find('Description').text
            isRangeWithBooleanCandidate = ("disable" in value.find('Description').text.lower() and isBooleanCandidate) or isRangeWithBooleanCandidate
        parameterDictionary['values'] = values
        isEnumCandidate = enumableValuesCounter == numberOfParameterValues
        isBooleanCandidate = isEnumCandidate and numberOfParameterValues == 2
        isRangeWithBooleanCandidate = (numberOfParameterValues == 2) and (enumableValuesCounter == 1) and isRangeWithBooleanCandidate
        isOnlyRange = numberOfParameterValues == 1 or (isOnlyRangeCandidate and numberOfParameterValues == 2)
        if isRangeWithBooleanCandidate:
            parameterDictionary['type'] = "boolRange"
        elif isBooleanCandidate:
            parameterDictionary['type'] = "boolean"
        elif isEnumCandidate:
            parameterDictionary['type'] = "enum"
        elif isOnlyRange:
            parameterDictionary['type'] = "range"
        else:
            parameterDictionary['type'] = "unknown"
            print(f"Preference type for parameter no. {parameterDictionary['parameterNumber']} couldn't be resolved. Please check it.")

        parametersList.append(parameterDictionary)

    return parametersList

def writeToGroovyFile(parametersList, outputFilename):
    formatter = OutputFormatter(outputFilename + '.groovy')
    firstLoop = True
    formatter.writeLine("private getParameterMap() {[", forceCurly=True)
    formatter.addIndentation()
    for parameter in parametersList:
        if not firstLoop:
            formatter.writeLine(',', applyIndentation=False)
            
        formatter.writeLine('[')
        formatter.addIndentation()

        string = ""
        string += "name: \"" + parameter['name'] + "\", "
        string += "key: \"" + parameter['key'] + "\", "
        string += "type: \"" + parameter['type'] + "\","
        formatter.writeLine(string)

        string = ""
        string += "parameterNumber: " + parameter['parameterNumber'] + ", "
        string += "size: " + parameter['size'] + ", "
        string += "defaultValue: " + parameter['defaultValue'] + ","
        formatter.writeLine(string)

        string = ""
        if parameter['type'] == "boolRange":
            for key in parameter['values'].keys():
                if '..' in key:
                    range_ = key
                    # del parameter['values'][key]
                else:
                    disableValue = key
                    # del parameter['values'][key]

            parameter['range'] = range_
            string += "range: \"" + parameter['range'] + "\", "
            parameter['disableValue'] = disableValue
            string += "disableValue: " + parameter['disableValue'] + ", "
            formatter.writeLine(string)

        elif parameter['type'] == "boolean":
            optionInactive = list(parameter['values'].keys())[0]
            string += "optionInactive: " + optionInactive + ", "
            string += "inactiveDescription: \"" + parameter['values'][optionInactive] + "\","
            formatter.writeLine(string)
            string = ""
            optionActive = list(parameter['values'].keys())[1]
            string += "optionActive: " + optionActive + ", "
            string += "activeDescription: \"" + parameter['values'][optionActive] + "\","
            formatter.writeLine(string)

        elif parameter['type'] == "enum":
            string += "values: ["
            formatter.writeLine(string)

            formatter.addIndentation()
            firstOption = True
            for key in parameter['values'].keys():
                string = ""
                if not firstOption:
                    formatter.writeLine(',', applyIndentation=False)
                string += key + ": \"" + parameter['values'][key] + "\""
                firstOption = False
                formatter.writeLine(string, escapeChar='')
            formatter.writeLine('')
            formatter.removeIndentation()
            formatter.writeLine('],')

        elif parameter['type'] == "range":
            for key in parameter['values']:
                range_ = key
            parameter['range'] = range_
            string += "range: \"" + parameter['range'] + "\", "
            formatter.writeLine(string)
        else:
            pass
        
        string = ""
        string += "description: \"" + parameter['description'] + "\""
        formatter.writeLine(string)

        formatter.removeIndentation()
        formatter.writeLine(']', escapeChar='')
        firstLoop = False
    formatter.writeLine('')
    formatter.removeIndentation()
    formatter.writeLine("]}", escapeChar='', forceCurly=True)
    print(f'Output filename: {outputFilename}')

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--productData", nargs=1, help="Path to Product Data XML downloaded from device's page at products.z-wavealliance.org")
    arguments = parser.parse_args()

    if arguments.productData is None:
        print("You haven't provided path to Product Data XML. Aborting.")
        sys.exit("No path provided")

    try:
        productData = element_tree.parse(arguments.productData[0])
    except IOError:
        print("Couldn't find file at provided path. Aborting.")
        sys.exit("File doesn't exist")
    except Exception as e:
        print("Exception: {}".format(e))
        sys.exit()

    root = productData.getroot()
    parameterRoot = root.find('ConfigurationParameters')
    parametersList = createParametersList(parameterRoot)
    writeToGroovyFile(parametersList, root.find('Name').text.replace(' ',''))
    