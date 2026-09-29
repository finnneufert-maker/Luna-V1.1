'use strict'

const { createClient } = require('bedrock-protocol')
const { createChatHandler } = require('./chat')

const desiredWorld = (process.env.LUNA_WORLD_NAME || '').trim()
const directHost = (process.env.LUNA_BEDROCK_HOST || '').trim()
if (!desiredWorld && !directHost) {
  console.error('Setze LUNA_WORLD_NAME für eine eingeladene Welt oder LUNA_BEDROCK_HOST für einen erreichbaren Bedrock-Host.')
  process.exitCode = 1
} else {
  const connection = directHost
    ? { host: directHost, port: Number(process.env.LUNA_BEDROCK_PORT || 19132) }
    : { world: {
        pickSession: sessions => {
          const matches = sessions.filter(s => s.customProperties?.worldName === desiredWorld)
          if (matches.length !== 1) throw new Error('Welt nicht eindeutig gefunden: ' + desiredWorld)
          return matches[0]
        }
      } }
  if (directHost && (!Number.isInteger(connection.port) || connection.port < 1 || connection.port > 65535))
    throw new Error('Ungültiger LUNA_BEDROCK_PORT')
  const client = createClient({
    username: 'Luna', // Local name for authenticated account cache, not an impersonation.
    profilesFolder: './.luna-auth',
    ...connection
  })
  const chat = createChatHandler(client, { owner: process.env.LUNA_OWNER_NAME })
  client.on('join', () => console.log('Luna: Anmeldung abgeschlossen.'))
  client.on('spawn', () => console.log('Luna: In der Welt erschienen; Chat aktiv.'))
  client.on('text', chat.handle)
  client.on('error', error => console.error('Luna-Verbindung:', error.message))
  client.on('close', () => console.log('Luna: Verbindung beendet.'))
}
