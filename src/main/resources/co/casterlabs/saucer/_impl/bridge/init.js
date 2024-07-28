const SAUCER = window.saucer;

function deepFreeze(object) {
	for (const value of Object.values(object)) {
		if ((value && typeof value === "object") || typeof value === "function") {
			deepFreeze(value);
		}
	}

	return Object.freeze(object);
}

const RPC = {
	MUTATION_POLL_RATE: 150,
	
	__idx: 0,
	waiting: {},
	
	__wfmHandlers: {}, // "Watch for mutate handlers"

	send: function (data) {
		SAUCER.on_message(JSON.stringify(data));
	},
	sendWithPromise: async function (data) {
		const start = Date.now();
		const requestId = RPC.__idx++;
		try {
			return await new Promise((resolve, reject) => {
				RPC.waiting[requestId] = { resolve, reject };
				SAUCER.on_message(JSON.stringify({ ...data, requestId }));
			});
		} finally {
			// console.debug("[Saucer]", "RPC sendWithPromise took ", Date.now() - start);
			delete RPC.waiting[requestId];
		}
	},

	get: function (objectId, propertyName) {
		return RPC.sendWithPromise({ type: "GET", objectId, propertyName });
	},
	set: async function (objectId, propertyName, newValue) {
		await RPC.sendWithPromise({ type: "SET", objectId, propertyName, newValue }); // Wait for the set() to complete (or throw).
		return newValue;
	},
	invoke: function (objectId, functionName, arguments) {
		return RPC.sendWithPromise({ type: "INVOKE", objectId, functionName, arguments });
	},
	checkForMutations: function () {
		return RPC.sendWithPromise({ type: "CHECK_MUTATION" });
	},
};
Object.defineProperty(SAUCER, "__rpc", {
	value: RPC,
	writable: true,
	configurable: true,
});

const MESSAGES = {
	__idx: 0,
	__listeners: {},

	onMessage: function (callback) {
		if (typeof callback != "function") {
			throw "Callback must be a fuction";
		}

		const registrationId = MESSAGES.__idx++;

		MESSAGES.__listeners[registrationId] = callback;

		return registrationId;
	},

	off: function (registrationId) {
		delete MESSAGES.__listeners[registrationId];
	},

	emit: function (data) {
		if (typeof data == "undefined") {
			throw "Data cannot be undefined!";
		}
		RPC.send({ type: "MESSAGE", data });
	},

	__internal: function (data) {
		deepFreeze(data);

		Object.values(MESSAGES.__listeners).forEach((callback) => {
			try {
				callback(data);
			} catch (e) {
				console.error("[Saucer]", "A listener produced an exception: ");
				console.error(e);
			}
		});
	}

};
Object.defineProperty(SAUCER, "messages", {
	value: MESSAGES,
	writable: true,
	configurable: true,
});

Object.defineProperty(SAUCER, "close", {
	value: function () {
		RPC.send({ type: "CLOSE" });
	},
	writable: true,
	configurable: true,
});

async function checkForMutations() {
	try {
		const mutations = await RPC.checkForMutations();
		
		for (const [toBeSplit, value] of Object.entries(mutations)) {
			const [objectId, propertyName] = toBeSplit.split("|");
			
			for (const handler of Object.values(RPC.__wfmHandlers[objectId][propertyName])) {
				try {
					handler(value);
				} catch (e) {
					console.error("[Saucer]", "A mutation listener produced an exception: ");
					console.error(e);
				}
			}
		}
	} catch (e) {
		console.error("[Saucer]", "An error occurred whilst checking for mutations:");
		console.error(e);
	} finally {
		setTimeout(checkForMutations, RPC.MUTATION_POLL_RATE);
	}
}
checkForMutations();

const SAUCER_WINDOW_EDGE_TOP = 1 << 0;
const SAUCER_WINDOW_EDGE_BOTTOM = 1 << 1;
const SAUCER_WINDOW_EDGE_LEFT = 1 << 2;
const SAUCER_WINDOW_EDGE_RIGHT = 1 << 3;

const DIRECTION_TO_BITMASK_LUT = {
	n: SAUCER_WINDOW_EDGE_TOP,
	s: SAUCER_WINDOW_EDGE_BOTTOM,
	w: SAUCER_WINDOW_EDGE_LEFT,
	e: SAUCER_WINDOW_EDGE_RIGHT,
	nw: SAUCER_WINDOW_EDGE_TOP | SAUCER_WINDOW_EDGE_LEFT,
	ne: SAUCER_WINDOW_EDGE_TOP | SAUCER_WINDOW_EDGE_RIGHT,
	sw: SAUCER_WINDOW_EDGE_BOTTOM | SAUCER_WINDOW_EDGE_LEFT,
	se: SAUCER_WINDOW_EDGE_BOTTOM | SAUCER_WINDOW_EDGE_RIGHT,
};

window.addEventListener("load", () => {
	document.documentElement.addEventListener("mousedown", ({ target: targetElement }) => {
		const isTargetingDragger = Array.from(document.querySelectorAll("[x-webview-dragger]"))
			.some((dragger) => dragger == targetElement || dragger.contains(targetElement));

		if (isTargetingDragger) {
			SAUCER.start_drag();
			return; // DO NOT ATTEMPT TO RESIZE AFTER THIS!
		}

		const targetResizer = Array.from(document.querySelectorAll("[x-webview-resizer]"))
			.filter((resizer) => resizer == targetElement || resizer.contains(targetElement))[0];

		if (targetResizer) {
			const direction = targetResizer.getAttribute("x-webview-resizer");
			const bitmask = DIRECTION_TO_BITMASK_LUT[direction];

			if (!bitmask) {
				console.warn("[Saucer]", "Unrecognized resizer direction:", direction, ", ignoring.");
				return;
			}

			SAUCER.start_resize(bitmask);
		}
	});

	document.head.innerHTML = `
		<style x-webview-styling>
			[x-webview-resizer=n] {
				cursor: n-resize;	
			}
			[x-webview-resizer=s] {
				cursor: s-resize;	
			}
			[x-webview-resizer=w] {
				cursor: w-resize;	
			}
			[x-webview-resizer=e] {
				cursor: e-resize;	
			}

			[x-webview-resizer=nw] {
				cursor: nw-resize;	
			}
			[x-webview-resizer=ne] {
				cursor: ne-resize;	
			}

			[x-webview-resizer=sw] {
				cursor: sw-resize;	
			}
			[x-webview-resizer=se] {
				cursor: se-resize;	
			}
		</style>
	` + document.head.innerHTML;
});