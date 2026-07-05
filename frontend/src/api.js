const BASE = '/api/partite';

function csrfHeaders() {
  const token = document.querySelector('meta[name="_csrf"]');
  const header = document.querySelector('meta[name="_csrf_header"]');
  if (!token || !header) return {};
  return { [header.content]: token.content };
}

async function parseErrore(res) {
  const dati = await res.json().catch(() => ({}));
  if (dati.messaggio) return dati.messaggio;
  const primoErrore = Object.values(dati)[0];
  return primoErrore || 'Si è verificato un errore';
}

export async function fetchCommenti(partitaId) {
  const res = await fetch(`${BASE}/${partitaId}/commenti`, { credentials: 'same-origin' });
  if (!res.ok) throw new Error('Errore nel caricamento dei commenti');
  return res.json();
}

export async function creaCommento(partitaId, testo) {
  const res = await fetch(`${BASE}/${partitaId}/commenti`, {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
    body: JSON.stringify({ testo }),
  });
  if (!res.ok) throw new Error(await parseErrore(res));
  return res.json();
}

export async function modificaCommento(partitaId, id, testo) {
  const res = await fetch(`${BASE}/${partitaId}/commenti/${id}`, {
    method: 'PUT',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
    body: JSON.stringify({ testo }),
  });
  if (!res.ok) throw new Error(await parseErrore(res));
  return res.json();
}