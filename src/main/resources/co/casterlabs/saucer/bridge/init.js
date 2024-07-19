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