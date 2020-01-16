preferences {
	parameterMap.each {
		input (
				title: it.name,
				description: it.description,
				type: "paragraph",
				element: "paragraph"
		)

		switch(it.type) {
			case "boolRange":
				input(
					name: it.key + "Boolean", 
					type: "bool",
					title: "Enable",
					description: "If you disable this option, it will overwrite setting below.",
					defaultValue: it.defaultValue != it.disableValue,
					required: false
				)
				input(
					name: it.key, 
					type: "number",
					title: "Set value (range ${it.range})",
					defaultValue: it.defaultValue,
					range: it.range,
					required: false
				)
				break
			case "boolean":
				input(
					description: "Option enabled: ${it.activeDescription}\n" + 
						"Option disabled: ${it.inactiveDescription}" 
				)
				input(
					name: it.key, 
					type: "boolean",
					title: "Enable",
					defaultValue: it.defaultValue == it.activeOption,
					required: false
				)
				break
			case "enum":
				input(
					name: it.key, 
					title: "Select",
					type: "enum",
					options: it.values.values(),
					defaultValue: it.values[it.defaultValue],
					required: false
				)
				break
			case "range":
				input(
					name: it.key, 
					type: "number",
					title: "Set value (range ${it.range})",
					defaultValue: it.defaultValue,
					range: it.range,
					required: false
				)
				break
		}
	}
}

def installed() {
	state.currentPreferencesState = [:]
	parameterMap.each {
		state.currentPreferencesState."$it.key" = [:]
		state.currentPreferencesState."$it.key".value = getPreferenceValue(it)
		if (it.type == "boolRange" && getPreferenceValue(it) == it.disableValue) {
			state.currentPreferencesState."$it.key".status = "disabled"
		} else {
			state.currentPreferencesState."$it.key".status = "synced"
		}
	}
}

def updated() {
	parameterMap.each {
		if (state.currentPreferencesState."$it.key".value != settings."$it.key" && settings."$it.key") {
			log.debug "Preference ${it.key} has been updated from value: ${state.currentPreferencesState."$it.key".value} to ${settings."$it.key"}"
			state.currentPreferencesState."$it.key".status = "notSynced"
		} else if (!state.currentPreferencesState."$it.key".value) {
			log.warn "Preference ${it.key} no. ${it.parameterNumber} has no value. Please check preference declaration for errors."
		}
        if (it.type == "boolRange") {
            def preferenceName = it.key + "Boolean"
            if (!settings."$preferenceName" && settings."$preferenceName" != null) {
                state.currentPreferencesState."$it.key".status = "disabled"
            }
		}
	}
	syncConfiguration()
}

private syncConfiguration() {
	int value
    String parameterKey
	def commands = []
	parameterMap.each {
		if (state.currentPreferencesState."$it.key".status == "notSynced") {
            value = getCommandValue(it)
            commands += secure(zwave.configurationV2.configurationSet(scaledConfigurationValue: value, parameterNumber: it.parameterNumber, size: it.size))
		    commands += secure(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
		} else if (state.currentPreferencesState."$it.key".status == "disabled") {
            commands += secure(zwave.configurationV2.configurationSet(scaledConfigurationValue: value, parameterNumber: it.parameterNumber, size: it.size))
			commands += secure(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
            value = it.disableValue
        }
	}
	sendHubCommand(commands)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug "Configuration report: ${cmd}"
    def preference = parameterMap.find( {it.parameterNumber == cmd.parameterNumber} )
    def key = preference.key
    def preferenceValue = getPreferenceValue(preference, cmd.scaledConfigurationValue)
	if (settings."$key" == preferenceValue) {
		state.currentPreferencesState."$key".value = settings."$key"
		state.currentPreferencesState."$key".status = "synced"
	} else if (preference.type == "boolRange") {
        if (state.currentPreferencesState."$key".status == "disabled" && preferenceValue == preference.disableValue) {
            state.currentPreferencesState."$key".status = "synced"
        } else {
            runIn(5, "syncConfiguration", [overwrite: true])
        }
    } else {
		state.currentPreferencesState."$key"?.status = "notSynced"
		runIn(5, "syncConfiguration", [overwrite: true])
	}
}

private getPreferenceValue(preference, value = "default") {
    def integerValue = value == "default" ? preference.defaultValue : value.intValue()
	switch (preference.type) {
		case "enum":
			return preference.values[integerValue]
		case "boolean":
			return String.valueOf(preference.optionActive == integerValue)
		default:
			return integerValue
	}
}

private getCommandValue(preference) {
    def parameterKey = preference.key
	switch (preference.type) {
		case "enum":
			return preference.values.find { it.value == settings."$parameterKey" }?.key
		case "boolean":
			return settings."$parameterKey" ? preference.optionActive : preference.optionInactive
        case "boolRange":
            def parameterKeyBoolean = parameterKey + "Boolean"
            return settings."$parameterKeyBoolean" ? settings."$parameterKey" : preference.disableValue
		default:
			return settings."$parameterKey"
	}
}