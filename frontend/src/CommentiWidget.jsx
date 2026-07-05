import { useEffect, useState } from 'react';
import { fetchCommenti, creaCommento, modificaCommento } from './api.js';

function formattaData(iso) {
  const d = new Date(iso);
  return d.toLocaleString('it-IT', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

export default function CommentiWidget({ partitaId, userEmail }) {
  const [commenti, setCommenti] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errore, setErrore] = useState('');
  const [testo, setTesto] = useState('');
  const [inviando, setInviando] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [editTesto, setEditTesto] = useState('');

  const isAuthenticated = Boolean(userEmail);

  useEffect(() => {
    fetchCommenti(partitaId)
      .then(setCommenti)
      .catch((e) => setErrore(e.message))
      .finally(() => setLoading(false));
  }, [partitaId]);

  async function handleSubmit(e) {
    e.preventDefault();
    if (!testo.trim()) return;
    setErrore('');
    setInviando(true);
    try {
      const nuovo = await creaCommento(partitaId, testo.trim());
      setCommenti((prev) => [nuovo, ...prev]);
      setTesto('');
    } catch (e) {
      setErrore(e.message);
    } finally {
      setInviando(false);
    }
  }

  function iniziaModifica(commento) {
    setEditingId(commento.id);
    setEditTesto(commento.testo);
    setErrore('');
  }

  function annullaModifica() {
    setEditingId(null);
    setEditTesto('');
  }

  async function salvaModifica(id) {
    if (!editTesto.trim()) return;
    setErrore('');
    try {
      const aggiornato = await modificaCommento(partitaId, id, editTesto.trim());
      setCommenti((prev) => prev.map((c) => (c.id === id ? aggiornato : c)));
      setEditingId(null);
    } catch (e) {
      setErrore(e.message);
    }
  }

  if (loading) {
    return <p className="text-muted">Caricamento commenti...</p>;
  }

  return (
    <div className="commenti-widget">
      {errore && <div className="alert alert-danger">{errore}</div>}

      {!isAuthenticated && (
        <p><a href="/login">Accedi</a> per aggiungere un commento.</p>
      )}

      {commenti.map((c) => (
        <div className="card shadow-sm mb-3" key={c.id}>
          <div className="card-body">
            {editingId === c.id ? (
              <>
                <textarea
                  className="form-control mb-2"
                  rows="3"
                  value={editTesto}
                  onChange={(e) => setEditTesto(e.target.value)}
                />
                <button className="btn btn-primary btn-sm" onClick={() => salvaModifica(c.id)}>Salva</button>{' '}
                <button className="btn btn-secondary btn-sm" onClick={annullaModifica}>Annulla</button>
              </>
            ) : (
              <>
                <p className="mb-1">
                  <strong>{c.autoreNome} {c.autoreCognome}</strong>{' '}
                  <small className="text-muted">{formattaData(c.dataCommento)}</small>
                </p>
                <p>{c.testo}</p>
                {c.autoreEmail === userEmail && (
                  <button className="btn btn-secondary btn-sm" onClick={() => iniziaModifica(c)}>Modifica</button>
                )}
              </>
            )}
          </div>
        </div>
      ))}

      {commenti.length === 0 && <p>Nessun commento.</p>}

      {isAuthenticated && (
        <div className="card shadow-sm mt-3">
          <div className="card-body">
            <h3 className="h5">Aggiungi un commento</h3>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <textarea
                  className="form-control"
                  rows="3"
                  placeholder="Scrivi il tuo commento..."
                  value={testo}
                  onChange={(e) => setTesto(e.target.value)}
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary" disabled={inviando}>
                {inviando ? 'Invio...' : 'Invia'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}