const SAUCER = window.saucer;

const RPC = {
	idx: 0,
	waiting: {},
	
	send: function(data) {
		SAUCER.on_message(JSON.stringify(data));
	},
	sendWithPromise: function(data) {
		const requestId = RPC.idx++;
		return new Promise((resolve, reject) => {
			RPC.waiting[requestId] = {resolve, reject};
			SAUCER.on_message(JSON.stringify({ ...data, requestId }));
		})
		.finally(() => {
			delete RPC.waiting[requestId];
		});
	},
	
	get: function(objectId, propertyName) {
		return RPC.sendWithPromise({ type: "GET", objectId, propertyName });
	},
	set: function(objectId, propertyName, newValue) {
		RPC.send({ type: "SET", objectId, propertyName, newValue });
		return newValue;
	},
	invoke: function(objectId, functionName, arguments) {
		return RPC.sendWithPromise({ type: "INVOKE", objectId, functionName, arguments });
	},
};

Object.defineProperty(SAUCER, "__rpc", {
	value: RPC,
	writable: true,
	configurable: true,
});

Object.defineProperty(SAUCER, "close", {
	value: function() {
		RPC.send({ type: "CLOSE" });
	},
	writable: true,
	configurable: true,
});

const SAUCER_WINDOW_EDGE_TOP    = 1 << 0;
const SAUCER_WINDOW_EDGE_BOTTOM = 1 << 1;
const SAUCER_WINDOW_EDGE_LEFT   = 1 << 2;
const SAUCER_WINDOW_EDGE_RIGHT  = 1 << 3;

window.addEventListener("load", () => {
	document.documentElement.addEventListener("mousedown", (e) => {
		let saucerElement = e.target;
		while (saucerElement) {
			if (
				saucerElement.hasAttribute("x-webview-dragger") || 
				saucerElement.hasAttribute("x-webview-resizer")
			) {
				break;
			} else {
				saucerElement = saucerElement.parentElement;
			}
		}
		
		if (!saucerElement) return; // Didn't find anything, ignore.
		
		if (saucerElement.hasAttribute("x-webview-dragger")) {
			SAUCER.start_drag();
		} else { // It's a resizer.
			const direction = saucerElement.getAttribute("x-webview-resizer");
			switch (direction) {
				case "n": 
					SAUCER.start_resize(SAUCER_WINDOW_EDGE_TOP);
					break;
				case "s": 
					SAUCER.start_resize(SAUCER_WINDOW_EDGE_BOTTOM);
					break;
				case "w": 
					SAUCER.start_resize(SAUCER_WINDOW_EDGE_LEFT);
					break;
				case "e": 
					SAUCER.start_resize(SAUCER_WINDOW_EDGE_RIGHT);
					break;

				case "nw": 
					SAUCER.start_resize(SAUCER_WINDOW_EDGE_TOP | SAUCER_WINDOW_EDGE_LEFT);
					break;
				case "ne": 
					SAUCER.start_resize(SAUCER_WINDOW_EDGE_TOP | SAUCER_WINDOW_EDGE_RIGHT);
					break;
					
				case "sw": 
					SAUCER.start_resize(SAUCER_WINDOW_EDGE_BOTTOM | SAUCER_WINDOW_EDGE_LEFT);
					break;
				case "se": 
					SAUCER.start_resize(SAUCER_WINDOW_EDGE_BOTTOM | SAUCER_WINDOW_EDGE_RIGHT);
					break;
				
				default:
					console.warn("[Saucer]", "Unrecognized resizer direction:", direction, ", ignoring.");
					return;
			}
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