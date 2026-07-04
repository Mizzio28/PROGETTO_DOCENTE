/**
 * PartitaStats - Componente React (CDN) per statistiche partita
 * Viene caricato nella pagina dettaglio partita via <script> tag
 * Legge i data-attribute dall'elemento #partita-stats e renderizza
 */
(function() {
  const root = document.getElementById('partita-stats');
  if (!root) return;

  const goalsHome = parseInt(root.dataset.goalsHome) || 0;
  const goalsAway = parseInt(root.dataset.goalsAway) || 0;
  const squadraHome = root.dataset.squadraHome || 'Home';
  const squadraAway = root.dataset.squadraAway || 'Away';
  const stato = root.dataset.stato || 'SCHEDULED';

  if (stato !== 'PLAYED') {
    root.innerHTML = '<p style="color:#888; font-style:italic;">Partita non ancora disputata.</p>';
    return;
  }

  const maxGoals = Math.max(goalsHome, goalsAway, 1);
  const pctHome = Math.round((goalsHome / maxGoals) * 100);
  const pctAway = Math.round((goalsAway / maxGoals) * 100);

  let risultato = 'Pareggio';
  let colore = '#888';
  if (goalsHome > goalsAway) { risultato = squadraHome + ' vince'; colore = '#1a6b2e'; }
  else if (goalsAway > goalsHome) { risultato = squadraAway + ' vince'; colore = '#1a6b2e'; }

  root.innerHTML = `
    <div style="background:white;border-radius:8px;padding:20px;box-shadow:0 2px 6px rgba(0,0,0,.1);margin-top:16px">
      <h3 style="color:#1a6b2e;margin-bottom:16px">📊 Statistiche partita</h3>
      <div style="display:flex;align-items:center;gap:16px;margin-bottom:12px">
        <span style="width:140px;text-align:right;font-weight:bold">${squadraHome}</span>
        <div style="flex:1;background:#eee;border-radius:4px;height:22px;overflow:hidden">
          <div style="width:${pctHome}%;background:#1a6b2e;height:100%;border-radius:4px;transition:width .6s"></div>
        </div>
        <span style="font-size:1.4rem;font-weight:bold;min-width:40px;text-align:center">${goalsHome}</span>
      </div>
      <div style="display:flex;align-items:center;gap:16px;margin-bottom:16px">
        <span style="width:140px;text-align:right;font-weight:bold">${squadraAway}</span>
        <div style="flex:1;background:#eee;border-radius:4px;height:22px;overflow:hidden">
          <div style="width:${pctAway}%;background:#c0392b;height:100%;border-radius:4px;transition:width .6s"></div>
        </div>
        <span style="font-size:1.4rem;font-weight:bold;min-width:40px;text-align:center">${goalsAway}</span>
      </div>
      <p style="text-align:center;font-weight:bold;color:${colore}">${risultato}</p>
    </div>
  `;
})();
