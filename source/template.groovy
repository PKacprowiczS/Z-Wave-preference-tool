preferences {
	// Preferences template begin
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
					type: "paragraph",
					element: "paragraph",
					description: "Option enabled: ${it.activeDescription}\n" + 
						"Option disabled: ${it.inactiveDescription}" 
				)
				input(
					name: it.key, 
					type: "bool",
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
					options: it.values,
					defaultValue: it.defaultValue,
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
	// Preferences template end
}

def installed() {
	// Preferences template begin
	state.currentPreferencesState = [:]
	parameterMap.each {
		state.currentPreferencesState."$it.key" = [:]
		state.currentPreferencesState."$it.key".value = getPreferenceValue(it)
		if (it.type == "boolRange" && getPreferenceValue(it) == it.disableValue) {
			state.currentPreferencesState."$it.key".status = "disablePending"
		} else {
			state.currentPreferencesState."$it.key".status = "synced"
		}
	}
	readConfigurationFromTheDevice();
	// Preferences template end
}

def updated() {
	// Preferences template begin
	parameterMap.each {
		if (isPreferenceChanged(it)) {
			log.debug "Preference ${it.key} has been updated from value: ${state.currentPreferencesState."$it.key".value} to ${settings."$it.key"}"
			state.currentPreferencesState."$it.key".status = "syncPending"
			if (it.type == "boolRange") {
				def preferenceName = it.key + "Boolean"
				if (notNullCheck(settings."$preferenceName")) {
					if (!settings."$preferenceName") {
						state.currentPreferencesState."$it.key".status = "disablePending"
					} else if (state.currentPreferencesState."$it.key".status == "disabled") {
						state.currentPreferencesState."$it.key".status = "syncPending"
					}
				} else {
					state.currentPreferencesState."$it.key".status = "syncPending"
				}
			}
		} else if (state.currentPreferencesState."$it.key".value == null) {
			log.warn "Preference ${it.key} no. ${it.parameterNumber} has no value. Please check preference declaration for errors."
		}
	}
	syncConfiguration()
	// Preferences template end
}

private readConfigurationFromTheDevice() {
	def commands = []
	parameterMap.each {
		state.currentPreferencesState."$it.key".status = "reverseSyncPending"
		commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
	}
	sendHubCommand(commands)
}

private syncConfiguration() {
	def commands = []
	parameterMap.each {
		if (state.currentPreferencesState."$it.key".status == "syncPending") {
            commands += encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: getCommandValue(it), parameterNumber: it.parameterNumber, size: it.size))
		    commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
		} else if (state.currentPreferencesState."$it.key".status == "disablePending") {
			commands += encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: it.disableValue, parameterNumber: it.parameterNumber, size: it.size))
			commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
		}
	}
	sendHubCommand(commands)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	// Preferences template begin
	log.debug "Configuration report: ${cmd}"
	def preference = parameterMap.find( {it.parameterNumber == cmd.parameterNumber} )
	def key = preference.key
	def preferenceValue = getPreferenceValue(preference, cmd.scaledConfigurationValue)

	if(state.currentPreferencesState."$key".status == "reverseSyncPending"){
		log.debug "reverseSyncPending"
		state.currentPreferencesState."$key".value = preferenceValue
		state.currentPreferencesState."$key".status = "synced"
	} else {
		if (settings."$key" == preferenceValue) {
			state.currentPreferencesState."$key".value = settings."$key"
			state.currentPreferencesState."$key".status = "synced"
		} else if (preference.type == "boolRange") {
			if (state.currentPreferencesState."$key".status == "disablePending" && preferenceValue == preference.disableValue) {
				state.currentPreferencesState."$key".status = "disabled"
			} else {
				runIn(5, "syncConfiguration", [overwrite: true])
			}
		} else {
			state.currentPreferencesState."$key"?.status = "syncPending"
			runIn(5, "syncConfiguratioWn", [overwrite: true])
		}
	}
	// Preferences template end
}

private getPreferenceValue(preference, value = "default") {
	def integerValue = value == "default" ? preference.defaultValue : value.intValue()
	switch (preference.type) {
		case "enum":
			return String.valueOf(integerValue)
		case "boolean":
			return String.valueOf(preference.optionActive == integerValue)
		default:
			return integerValue
	}
}

private getCommandValue(preference) {
	def parameterKey = preference.key
	switch (preference.type) {
		// boolean values are returned as strings from the UI preferences: 'true', 'false'
		case "boolean":
			return settings."$parameterKey" == "true" ? preference.optionActive : preference.optionInactive
		case "boolRange":
			def parameterKeyBoolean = parameterKey + "Boolean"
			return !notNullCheck(settings."$parameterKeyBoolean") || settings."$parameterKeyBoolean" == "true" ? settings."$parameterKey" : preference.disableValue
		case "range":
			return settings."$parameterKey"
		default:
			return Integer.parseInt(settings."$parameterKey")
	}
}

private isPreferenceChanged(preference) {
	if (notNullCheck(settings."$preference.key")) {
		if (preference.type == "boolRange") {
			def boolName = preference.key + "Boolean"
			if (state.currentPreferencesState."$preference.key".status == "disabled") {
				return settings."$boolName"
			} else {
				return state.currentPreferencesState."$preference.key".value != settings."$preference.key" || !settings."$boolName"
			}
		} else {
			return state.currentPreferencesState."$preference.key".value != settings."$preference.key"
		}
	} else {
		return false
	}
}

private notNullCheck(value) {
	return value != null
}

private encap(cmd, endpoint = null) {
	if (cmd) {
		if (endpoint) {
			cmd = zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpoint).encapsulate(cmd)
		}

		if (zwaveInfo.zw.endsWith("s")) {
			zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		} else {
			cmd.format()
		}
	}
}