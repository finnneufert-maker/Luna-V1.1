'use strict'

const MAX_REPLY = 170
const COOLDOWN_MS = 2500
const COMMAND = /^\s*luna\s*[,!:]?\s*(.*)$/i

function respond (text, state) {
  const match = String(text || '').match(COMMAND)
  if (!match) return null
  const question = match[1].trim().toLocaleLowerCase('de-DE')
  if (/^(hilfe|befehle|was kannst du)\b/.test(question))
    return 'Sag: Luna, hallo / status / folge / stopp / bauidee. Bewegung und Bauen sind noch in Entwicklung.'
  if (/^(status|wo bist du)\b/.test(question))
    return state.mode === 'waiting' ? 'Ich bin hier und warte.' : 'Ich habe den Folgewunsch gespeichert; Bewegung ist noch nicht aktiv.'
  if (/^(stopp?|halt|warte|pause)\b/.test(question)) {
    state.mode = 'waiting'
    return 'Alles klar, ich warte hier.'
  }
  if (/^(folge|komm mit|begleite mich)\b/.test(question)) {
    state.mode = 'follow_requested'
    return 'Ich habe dich gehört. Folgen wird erst aktiviert, wenn die Bewegung geprüft ist.'
  }
  if (/\b(bau|baue|haus|brücke|bruecke)\b/.test(question)) {
    state.mode = 'waiting'
    return 'Zeig mir zuerst einen Bauplatz. Automatisch bauen ist noch nicht freigegeben.'
  }
  if (/\b(hallo|hi|hey)\b/.test(question)) return 'Hallo! Ich bin hier und lese den Chat mit.'
  if (/\b(witz|scherz)\b/.test(question)) return 'Warum baut der Creeper kein Haus? Er hat bei jedem Entwurf eine zündende Idee.'
  return 'Ich habe dich gelesen. Gerade kann ich im Spiel nur einfache Befehle verstehen.'
}

function createChatHandler (client, options = {}) {
  const state = { mode: 'waiting', lastReply: 0 }
  const clock = options.clock || Date.now
  const owner = String(options.owner || '').trim().toLocaleLowerCase('de-DE')
  function handle (packet) {
    if (!packet || packet.type !== 'chat' || !packet.source_name || !packet.message) return
    if (packet.source_name === client.username) return
    if (owner && packet.source_name.toLocaleLowerCase('de-DE') !== owner) return
    const reply = respond(packet.message, state)
    if (!reply || clock() - state.lastReply < COOLDOWN_MS) return
    state.lastReply = clock()
    client.queue('text', {
      type: 'chat', needs_translation: false, source_name: client.username,
      xuid: '', platform_chat_id: '', filtered_message: '', message: reply.slice(0, MAX_REPLY)
    })
  }
  return { handle, state }
}

module.exports = { respond, createChatHandler }
