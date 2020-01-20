private getParameterMap() {[
	[
		name: "Force calibration", key: "forceCalibration", type: "enum",
		parameterNumber: 150, size: 1, defaultValue: 0,
		values: [
			0: "device is not calibrated",
			1: "device is calibrated",
			2: "force device calibration"			
		],
		description: "By setting this parameter to 2 the device enters the calibration mode. The parameter relevant only if the device is set to work in positioning mode (parameter 151 set to 1, 2 or 4)."
	],
	[
		name: "Operating mode", key: "operatingMode", type: "enum",
		parameterNumber: 151, size: 1, defaultValue: 1,
		values: [
			1: "roller blind (with positioning)",
			2: "Venetian blind (with positioning)",
			3: "gate (without positioning)",
			4: "gate (with positioning)",
			5: "roller blind with built-in driver",
			6: "roller blind with built-in driver (impulse)"			
		],
		description: "This parameter allows adjusting operation according to the connected device."
	],
	[
		name: "Venetian blind - time of full turn of the slats", key: "venetianBlind-TimeOfFullTurnOfTheSlats", type: "range",
		parameterNumber: 152, size: 4, defaultValue: 150,
		range: "0..90000", 
		description: "For Venetian blinds (parameter 151 set to 2) the parameter determines time of full turn cycle of the slats. For gates (parameter 151 set to 3 or 4) the parameter determines time after which open gate will start closing automatically (if set to 0, gate will not close). The parameter is irrelevant for other modes."
	],
	[
		name: "Set slats back to previous position", key: "setSlatsBackToPreviousPosition", type: "enum",
		parameterNumber: 153, size: 1, defaultValue: 1,
		values: [
			0: "slats return to previously set position only in case of the main controller operation.",
			1: "slats return to previously set position in case of the main controller operation, momentary switch operation, or when the limit switch is reached.",
			2: "slats return to previously set position in case of the main controller operation, momentary switch operation, when the limit switch is reached or after receiving the Switch Multilevel Stop control frame"			
		],
		description: "For Venetian blinds (parameter 151 set to 2) the parameter determines slats positioning in various situations. The parameter is irrelevant for other modes.  NOTE: If parameter 20 is set to 1 (toggle switch), change value of parameter 153 to 0 for slats to work properly."
	],
	[
		name: "Delay motor stop after reaching end switch", key: "delayMotorStopAfterReachingEndSwitch", type: "range",
		parameterNumber: 154, size: 2, defaultValue: 10,
		range: "0..600", 
		description: "For blinds (parameter 151 set to 1, 2, 5 or 6) the parameter determines the time after which the motor will be stopped after end switch contacts are closed. For gates (parameter 151 set to 3 or 4) the parameter determines time after which the gate will start closing automatically if S2 contacts are opened (if set to 0, gate will not close)."
	],
	[
		name: "Motor operation detection", key: "motorOperationDetection", type: "boolRange",
		parameterNumber: 155, size: 2, defaultValue: 10,
		range: "1..255", disableValue: 0,
		description: "Power threshold to be interpreted as reaching a limit switch."
	],
	[
		name: "Time of up movement", key: "timeOfUpMovement", type: "range",
		parameterNumber: 156, size: 4, defaultValue: 6000,
		range: "1..90000", 
		description: "This parameter determines the time needed for roller blinds to reach the top. For modes with positioning value is set automatically during calibration, otherwise it must be set manually."
	],
	[
		name: "Time of down movement", key: "timeOfDownMovement", type: "range",
		parameterNumber: 157, size: 4, defaultValue: 6000,
		range: "1..90000", 
		description: "This parameter determines time needed for roller blinds to reach the bottom. For modes with positioning value is set automatically during calibration, otherwise it must be set manually."
	],
	[
		name: "Switch type", key: "switchType", type: "enum",
		parameterNumber: 20, size: 1, defaultValue: 2,
		values: [
			0: "momentary switches",
			1: "toggle switches",
			2: "single, momentary switch (the switch should be connected to S1 terminal)"			
		],
		description: "This parameter defines as what type the device should treat the switch connected to the S1 and S2 terminals. This parameter is not relevant in gate operating modes (parameter 151 set to 3 or 4). In this case switch always works as a momentary and has to be connected to S1 terminal."
	],
	[
		name: "Inputs orientation", key: "inputsOrientation", type: "boolean",
		parameterNumber: 24, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "default (S1 - 1st channel, S2 - 2nd channel)",
		optionActive: 1, activeDescription: "reversed (S1 - 2nd channel, S2 - 1st channel)",
		description: "This parameter allows reversing the operation of switches connected to S1 and S2 without changing the wiring."
	],
	[
		name: "Outputs orientation", key: "outputsOrientation", type: "boolean",
		parameterNumber: 25, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "default (Q1 - 1st channel, Q2 - 2nd channel)",
		optionActive: 1, activeDescription: "reversed (Q1 - 2nd channel, Q2 - 1st channel)",
		description: "This parameter allows reversing the operation of Q1 and Q2 without changing the wiring (in case of invalid motor connection) to ensure proper operation."
	],
	[
		name: "Alarm configuration - 1st slot", key: "alarmConfiguration-1StSlot", type: "enum",
		parameterNumber: 30, size: 4, defaultValue: 0,
		values: [
			1: "Notification Type",
			2: "Notification Status",
			3: "Event/State Parameters",
			4: "action: 0- no action, 1-open blinds, 2-close blinds"			
		],
		description: "This parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification.  NOTE: Alarm with lower number has the higher priority, thus first alarm will override other alarms with the same type.  NOTE: Setting Notification Value to 0xFF will result in launching the action twice: when alarm occurs and is cancelled."
	],
	[
		name: "Alarm configuration - 2nd slot (Water)", key: "alarmConfiguration-2NdSlot(Water)", type: "enum",
		parameterNumber: 31, size: 4, defaultValue: 0,
		values: [
			1: "Notification Type",
			2: "Notification Status",
			3: "Event/State Parameters",
			4: "action: 0- no action, 1-open blinds, 2-close blinds"			
		],
		description: "This parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification."
	],
	[
		name: "Alarm configuration - 3rd slot (Smoke)", key: "alarmConfiguration-3RdSlot(Smoke)", type: "enum",
		parameterNumber: 32, size: 4, defaultValue: 0,
		values: [
			1: "Notification Type",
			2: "Notification Status",
			3: "Event/State Parameters",
			4: "action: 0- no action, 1-open blinds, 2-close blinds"			
		],
		description: "This parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification."
	],
	[
		name: "Alarm configuration - 4th slot (CO)", key: "alarmConfiguration-4ThSlot(Co)", type: "enum",
		parameterNumber: 33, size: 4, defaultValue: 0,
		values: [
			1: "Notification Type",
			2: "Notification Status",
			3: "Event/State Parameters",
			4: "action: 0- no action, 1-open blinds, 2-close blinds"			
		],
		description: "This parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification."
	],
	[
		name: "Alarm configuration - 5th slot (Heat)", key: "alarmConfiguration-5ThSlot(Heat)", type: "enum",
		parameterNumber: 34, size: 4, defaultValue: 0,
		values: [
			1: "Notification Type",
			2: "Notification Status",
			3: "Event/State Parameters",
			4: "action: 0- no action, 1-open blinds, 2-close blinds"			
		],
		description: "This parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification."
	],
	[
		name: "S1 switch - scenes sent", key: "s1Switch-ScenesSent", type: "enum",
		parameterNumber: 40, size: 1, defaultValue: 0,
		values: [
			1: "Key pressed 1 time",
			2: "Key pressed 2 times",
			4: "Key pressed 3 times",
			8: "Key hold down and key released"			
		],
		description: "This parameter determines which actions result in sending scene IDs assigned to them.  NOTE: Parameter 40 values may be combined, e.g. 1+2=3 means that scenes for single and double click are sent.  NOTE: Enabling triple click for S1 in parameter 40 disables the ability to add/remove via S1."
	],
	[
		name: "S2 switch - scenes sent", key: "s2Switch-ScenesSent", type: "enum",
		parameterNumber: 41, size: 1, defaultValue: 0,
		values: [
			1: "Key pressed 1 time",
			2: "Key pressed 2 times",
			4: "Key pressed 3 times",
			8: "Key hold down and key released"			
		],
		description: "This parameter determines which actions result in sending scene IDs assigned to them.  NOTE: Parameter 41 values may be combined, e.g. 1+2=3 means that scenes for single and double click are sent."
	],
	[
		name: "Measuring power consumed by the device itself", key: "measuringPowerConsumedByTheDeviceItself", type: "boolean",
		parameterNumber: 60, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "function inactive",
		optionActive: 1, activeDescription: "function active",
		description: "This parameter determines whether the power metering should include the amount of active power consumed by the device itself."
	],
	[
		name: "Power reports - on change", key: "powerReports-OnChange", type: "boolRange",
		parameterNumber: 61, size: 2, defaultValue: 15,
		range: "1..500", disableValue: 0, 
		description: "This parameter determines the minimum change in consumed power that will result in sending new power report to the main controller. For loads under 50W, the parameter is not relevant and reports are sent every 5W change. Power report are sent no often then every 30 seconds."
	],
	[
		name: "Power reports - periodic", key: "powerReports-Periodic", type: "boolRange",
		parameterNumber: 62, size: 2, defaultValue: 3600,
		range: "30..32400", disableValue: 0, 
		description: "This parameter determines in what time intervals the periodic power reports are sent to the main controller. Periodic reports do not depend on power change (parameter 61)."
	],
	[
		name: "Energy reports - on change", key: "energyReports-OnChange", type: "boolRange",
		parameterNumber: 65, size: 2, defaultValue: 10,
		range: "1..500", disableValue: 0, 
		description: "This parameter determines the minimum change in consumed energy that will result in sending new energy report to the main controller."
	],
	[
		name: "Energy reports - periodic", key: "energyReports-Periodic", type: "boolRange",
		parameterNumber: 66, size: 2, defaultValue: 3600,
		range: "30..32400", disableValue: 0, 
		description: "This parameter determines in what time intervals the periodic energy reports are sent to the main controller. Periodic reports do not depend on energy change (parameter 65)."
	]	
]}