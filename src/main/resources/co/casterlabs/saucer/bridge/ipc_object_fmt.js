const RPC = window.saucer.__rpc;

const id = %s;
const path = %s.split(".");

const functionNames = %s;
const propertyNames = %s;

const object = {};

for (const functionName of functionNames) {
	Object.defineProperty(object, functionName, {
		value: function () {
			return RPC.invoke(id, functionName, Array.from(arguments));
		},
	});
}

for (const propertyName of propertyNames) {
	Object.defineProperty(object, propertyName, {
		value: undefined, // Placeholder so that the Dev Tools will see this property.
		writable: true,
		configurable: true,
	});
}

const proxy = new Proxy(object, {
    get(obj, propertyName) {
        if (typeof obj[propertyName] !== "undefined") {
            return obj[propertyName];
        }

        return RPC.get(id, propertyName);
    },
    set(obj, property, value) {
        RPC.set(id, propertyName, value);
        return value;
    },
});

const propertyName = path.pop();

// Resolve the root object.
let root = window;
for (const part of path) {
	root = root[part];
}

Object.defineProperty(root, propertyName, {
	value: proxy,
	writable: true,
	configurable: true,
});