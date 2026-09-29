'use strict'
const { test } = require('node:test')
const assert = require('node:assert/strict')
const { respond, createChatHandler } = require('./chat')

test('antwortet nur auf Luna-Anrede und stoppt', () => {
  const state = { mode: 'follow_requested' }
  assert.equal(respond('Hallo zusammen', state), null)
  assert.equal(respond('Luna, stopp', state), 'Alles klar, ich warte hier.')
  assert.equal(state.mode, 'waiting')
})
test('sendet nicht auf eigene Nachrichten und begrenzt Antworten', () => {
  let now = 10000
  const sent = []
  const client = { username: 'Luna', queue: (_, packet) => sent.push(packet) }
  const chat = createChatHandler(client, { clock: () => now })
  chat.handle({ type: 'chat', source_name: 'Luna', message: 'Luna hi' })
  chat.handle({ type: 'chat', source_name: 'Finn', message: 'Luna hi' })
  chat.handle({ type: 'chat', source_name: 'Finn', message: 'Luna hi' })
  now += 3000
  chat.handle({ type: 'chat', source_name: 'Finn', message: 'Luna, stopp' })
  assert.equal(sent.length, 2)
  assert.equal(chat.state.mode, 'waiting')
})
test('nur die freigegebene Person kann steuern, status und stopp funktionieren', () => {
  const sent = []
  const client = { username: 'Luna', queue: (_, packet) => sent.push(packet.message) }
  let now = 10000
  const chat = createChatHandler(client, { owner: 'Finn', clock: () => now })
  chat.handle({ type: 'chat', source_name: 'Gast', message: 'Luna, folge' })
  assert.equal(chat.state.mode, 'waiting')
  chat.handle({ type: 'chat', source_name: 'Finn', message: 'Luna, folge' })
  assert.equal(chat.state.mode, 'follow_requested')
  now += 3000
  chat.handle({ type: 'chat', source_name: 'Finn', message: 'Luna, status' })
  assert.match(sent[1], /noch nicht aktiv/)
  now += 3000
  chat.handle({ type: 'chat', source_name: 'Finn', message: 'Luna, stopp' })
  assert.equal(chat.state.mode, 'waiting')
})
