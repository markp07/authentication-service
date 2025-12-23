// Mock for @parcel/watcher to avoid native dependency issues in Jest/CI
// This is a simplified mock that provides the basic API without native bindings

const EventEmitter = require('events');

class MockWatcher extends EventEmitter {
  constructor() {
    super();
  }

  close() {
    return Promise.resolve();
  }
}

module.exports = {
  subscribe: jest.fn((dir, fn, opts) => {
    const watcher = new MockWatcher();
    return Promise.resolve({
      unsubscribe: jest.fn(() => Promise.resolve()),
    });
  }),
  getEventsSince: jest.fn(() => Promise.resolve([])),
  writeSnapshot: jest.fn(() => Promise.resolve()),
};

