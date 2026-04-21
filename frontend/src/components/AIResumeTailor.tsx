import React, { useState } from 'react';
import axios from 'axios';

const API = axios.create({ baseURL: 'https://job-search-backend.proudtree-37f0d902.northeurope.azurecontainerapps.io' });
API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

interface Props {
  jobTitle: string;
  company: string;
  jobDescription: string;
  onClose: () => void;
}

export default function AIResumeTailor({ jobTitle, company, jobDescription, onClose }: Props) {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<any>(null);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('analysis');

  const handleTailor = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await API.post('/api/ai/tailor-resume', {
        jobTitle, company, jobDescription
      });
      setResult(res.data);
    } catch (e: any) {
      setError(e.response?.data?.error || 'Please add your resume in Profile → Resume tab first!');
    } finally {
      setLoading(false); }
  };

  const handleDownload = () => {
    const lines: string[] = result.tailoredResume.split('\n');
    let html = `<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: Arial, sans-serif; font-size: 10.5pt; color: #000; padding: 36px 48px; line-height: 1.4; }
  .name { font-size: 20pt; font-weight: bold; text-align: center; margin-bottom: 4px; }
  .contact { text-align: center; font-size: 9.5pt; color: #333; margin-bottom: 14px; }
  .section { margin-top: 12px; margin-bottom: 4px; font-size: 11pt; font-weight: bold; text-transform: uppercase; border-bottom: 1.5px solid #000; padding-bottom: 2px; }
  .job-header { font-weight: bold; margin-top: 8px; margin-bottom: 1px; }
  .job-sub { font-style: italic; color: #333; margin-bottom: 3px; font-size: 10pt; }
  .bullet { padding-left: 16px; margin: 2px 0; }
  .bullet::before { content: "• "; }
  p { margin: 2px 0; }
  @media print { body { padding: 24px 36px; } }
</style>
</head>
<body>`;

    const sections = ['EDUCATION','EXPERIENCE','TECHNICAL SKILLS','SKILLS','PROJECTS','CERTIFICATIONS','SUMMARY','PROFESSIONAL SUMMARY'];
    let isFirst = true;

    lines.forEach((line: string, idx: number) => {
      const trimmed = line.trim();
      if (!trimmed) return;

      if (isFirst) {
        html += `<div class="name">${trimmed}</div>`;
        isFirst = false;
        return;
      }
      if (trimmed.includes('@') && trimmed.includes('|')) {
        html += `<div class="contact">${trimmed}</div>`;
        return;
      }
      if (trimmed.includes('github.com') || trimmed.includes('.github.io')) {
        html += `<div class="contact">${trimmed}</div>`;
        return;
      }
      if (sections.some(s => trimmed.toUpperCase().startsWith(s))) {
        html += `<div class="section">${trimmed}</div>`;
        return;
      }
      if (trimmed.startsWith('•') || trimmed.startsWith('-')) {
        html += `<div class="bullet">${trimmed.replace(/^[•\-]\s*/, '')}</div>`;
        return;
      }
      if (trimmed.match(/\d{4}/) && (trimmed.includes('|') || trimmed.includes('–') || trimmed.includes('-'))) {
        html += `<div class="job-header">${trimmed}</div>`;
        return;
      }
      if (trimmed.length < 60 && idx > 0 && !trimmed.includes(':')) {
        html += `<div class="job-sub">${trimmed}</div>`;
        return;
      }
      html += `<p>${trimmed}</p>`;
    });

    html += '</body></html>';
    const blob = new Blob([html], { type: 'text/html' });
    const url = URL.createObjectURL(blob);
    const printWindow = window.open(url, '_blank');
    if (printWindow) {
      printWindow.onload = () => {
        setTimeout(() => { printWindow.print(); }, 500);
      };
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 80) return '#2e7d32';
    if (score >= 60) return '#f57c00';
    return '#c62828';
  };

  const getScoreBg = (score: number) => {
    if (score >= 80) return '#e8f5e9';
    if (score >= 60) return '#fff3e0';
    return '#ffebee';
  };

  return (
    <div style={s.overlay}>
      <div style={s.modal}>
        <div style={s.header}>
          <div>
            <h2 style={s.title}>🤖 AI Resume Tailor</h2>
            <p style={s.subtitle}>{jobTitle} at {company}</p>
          </div>
          <button style={s.closeBtn} onClick={onClose}>✕</button>
        </div>

        {!result ? (
          <div style={s.body}>
            <div style={s.infoBox}>
              <h3 style={s.infoTitle}>How it works:</h3>
              <div style={s.steps}>
                {['Analyzes your real resume from your profile',
                  'Compares against this job description',
                  'Shows your ATS fit score & missing keywords',
                  'Rewrites your resume to be 100% fit'].map((step, i) => (
                  <div key={i} style={s.step}>
                    <span style={s.stepNum}>{i+1}</span>
                    <span>{step}</span>
                  </div>
                ))}
              </div>
            </div>
            {error && <div style={s.error}>⚠️ {error}</div>}
            <button style={loading ? s.loadingBtn : s.tailorBtn}
              onClick={handleTailor} disabled={loading}>
              {loading ? '🤖 AI is analyzing & tailoring...' : '🚀 Analyze & Tailor My Resume'}
            </button>
            <p style={s.note}>📌 Your actual resume from Profile will be used</p>
          </div>
        ) : (
          <div style={s.body}>
            <div style={s.scoreRow}>
              <div style={{...s.scoreCard, background: getScoreBg(result.beforeScore)}}>
                <div style={{...s.scoreBig, color: getScoreColor(result.beforeScore)}}>{result.beforeScore}%</div>
                <div style={s.scoreLabel}>Current Fit</div>
                <div style={{fontSize:11, fontWeight:700, color: getScoreColor(result.beforeScore)}}>{result.fitLevel}</div>
              </div>
              <div style={s.arrowBox}>
                <div style={s.arrowBig}>→</div>
                <div style={s.improvement}>+{result.improvement}%</div>
              </div>
              <div style={{...s.scoreCard, background: getScoreBg(result.afterScore)}}>
                <div style={{...s.scoreBig, color: getScoreColor(result.afterScore)}}>{result.afterScore}%</div>
                <div style={s.scoreLabel}>After Tailoring</div>
                <div style={{fontSize:11, fontWeight:700, color: getScoreColor(result.afterScore)}}>{result.afterFitLevel}</div>
              </div>
            </div>

            {result.feedback && (
              <div style={s.feedbackBox}>
                <strong>📝 AI Feedback:</strong> {result.feedback}
              </div>
            )}

            <div style={s.tabs}>
              {['analysis','resume'].map(tab => (
                <button key={tab} style={activeTab === tab ? s.tabActive : s.tab}
                  onClick={() => setActiveTab(tab)}>
                  {tab === 'analysis' ? '📊 Analysis' : '📄 Tailored Resume'}
                </button>
              ))}
            </div>

            {activeTab === 'analysis' && (
              <div>
                <div style={s.keywordSection}>
                  <h4 style={{...s.kwTitle, color:'#2e7d32'}}>✅ Matched Keywords ({result.matchedKeywords?.length || 0})</h4>
                  <div style={s.kwList}>
                    {result.matchedKeywords?.map((kw: string) => (
                      <span key={kw} style={s.kwMatch}>{kw}</span>
                    ))}
                  </div>
                </div>
                <div style={s.keywordSection}>
                  <h4 style={{...s.kwTitle, color:'#c62828'}}>❌ Missing Keywords ({result.missingKeywords?.length || 0})</h4>
                  <div style={s.kwList}>
                    {result.missingKeywords?.map((kw: string) => (
                      <span key={kw} style={s.kwMissing}>{kw}</span>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'resume' && (
              <div>
                <textarea style={s.resumeText}
                  value={result.tailoredResume} readOnly rows={14} />
                <div style={s.actions}>
                  <button style={s.downloadBtn} onClick={handleDownload}>⬇️ Download as PDF</button>
                  <button style={s.retryBtn} onClick={() => setResult(null)}>🔄 Re-analyze</button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

const s: { [key: string]: React.CSSProperties } = {
  overlay: { position:'fixed', inset:0, background:'rgba(0,0,0,0.6)', zIndex:1000,
    display:'flex', alignItems:'center', justifyContent:'center', padding:16 },
  modal: { background:'white', borderRadius:16, width:'100%', maxWidth:640,
    maxHeight:'92vh', overflow:'auto', boxShadow:'0 20px 60px rgba(0,0,0,0.3)' },
  header: { padding:'20px 24px 16px', borderBottom:'1px solid #eee',
    display:'flex', justifyContent:'space-between', alignItems:'flex-start',
    position:'sticky', top:0, background:'white', zIndex:1 },
  title: { fontSize:20, fontWeight:700, margin:0, color:'#1e3a5f' },
  subtitle: { fontSize:13, color:'#666', margin:'3px 0 0' },
  closeBtn: { background:'none', border:'none', fontSize:20, cursor:'pointer', color:'#666' },
  body: { padding:24 },
  infoBox: { background:'#f0f4ff', borderRadius:12, padding:20, marginBottom:20 },
  infoTitle: { fontSize:15, fontWeight:700, color:'#1e3a5f', margin:'0 0 12px' },
  steps: { display:'flex', flexDirection:'column', gap:10 },
  step: { display:'flex', alignItems:'center', gap:10, fontSize:14, color:'#444' },
  stepNum: { width:24, height:24, borderRadius:'50%', background:'#2d6a9f',
    color:'white', display:'inline-flex', alignItems:'center', justifyContent:'center',
    fontSize:12, fontWeight:700, flexShrink:0 },
  error: { background:'#ffebee', color:'#c62828', padding:14, borderRadius:8,
    fontSize:14, marginBottom:16 },
  tailorBtn: { width:'100%', padding:'14px', background:'linear-gradient(135deg,#1e3a5f,#2d6a9f)',
    color:'white', border:'none', borderRadius:10, fontSize:16, fontWeight:700, cursor:'pointer' },
  loadingBtn: { width:'100%', padding:'14px', background:'#90a4ae', color:'white',
    border:'none', borderRadius:10, fontSize:15, fontWeight:600, cursor:'not-allowed' },
  note: { fontSize:12, color:'#999', textAlign:'center', marginTop:12 },
  scoreRow: { display:'flex', alignItems:'center', gap:12, marginBottom:16 },
  scoreCard: { flex:1, textAlign:'center', padding:'16px 8px', borderRadius:12 },
  scoreBig: { fontSize:36, fontWeight:800, lineHeight:1 },
  scoreLabel: { fontSize:12, color:'#666', margin:'4px 0' },
  arrowBox: { textAlign:'center' },
  arrowBig: { fontSize:24, color:'#666' },
  improvement: { fontSize:13, fontWeight:700, color:'#2e7d32' },
  feedbackBox: { background:'#f8f9ff', border:'1px solid #e0e8ff', borderRadius:8,
    padding:14, fontSize:14, color:'#444', marginBottom:16, lineHeight:1.6 },
  tabs: { display:'flex', gap:4, marginBottom:16 },
  tab: { padding:'8px 16px', background:'#f5f5f5', border:'1px solid #ddd',
    borderRadius:6, cursor:'pointer', fontSize:13, color:'#555' },
  tabActive: { padding:'8px 16px', background:'#2d6a9f', border:'1px solid #2d6a9f',
    borderRadius:6, cursor:'pointer', fontSize:13, color:'white', fontWeight:600 },
  keywordSection: { marginBottom:16 },
  kwTitle: { fontSize:14, fontWeight:600, margin:'0 0 8px' },
  kwList: { display:'flex', flexWrap:'wrap', gap:6 },
  kwMatch: { background:'#e8f5e9', color:'#2e7d32', padding:'4px 10px', borderRadius:20, fontSize:12, fontWeight:500 },
  kwMissing: { background:'#ffebee', color:'#c62828', padding:'4px 10px', borderRadius:20, fontSize:12, fontWeight:500 },
  resumeText: { width:'100%', padding:12, borderRadius:8, border:'1px solid #ddd',
    fontSize:12, fontFamily:'monospace', resize:'vertical', boxSizing:'border-box', lineHeight:1.6 },
  actions: { display:'flex', gap:12, marginTop:12 },
  downloadBtn: { flex:1, padding:12, background:'#2e7d32', color:'white',
    border:'none', borderRadius:8, fontSize:14, fontWeight:600, cursor:'pointer' },
  retryBtn: { padding:'12px 16px', background:'white', color:'#2d6a9f',
    border:'1.5px solid #2d6a9f', borderRadius:8, fontSize:13, fontWeight:600, cursor:'pointer' },
};
