import React, { useEffect, useState } from 'react';
import AIResumeTailor from '../components/AIResumeTailor';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const API = axios.create({ baseURL: 'http://localhost:8080' });
API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

interface Job {
  id: number;
  title: string;
  company: string;
  location: string;
  salary: string;
  jobType: string;
  experienceLevel: string;
  source: string;
  sourceUrl: string;
  skills: string[];
  description: string;
  postedAt: string;
}

export default function Jobs() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [aiJob, setAiJob] = useState<any>(null);
  const [pageSize, setPageSize] = useState(12);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState({
    jobType: 'ALL',
    experience: 'ALL',
    source: 'ALL',
    location: '',
    minSalary: '',
  });
  const navigate = useNavigate();
  const goToProfile = () => navigate('/profile');
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => { loadJobs(0); }, [pageSize]);

  const loadJobs = async (p: number) => {
    setLoading(true);
    try {
      const res = await API.get(`/api/jobs?page=${p}&size=${pageSize}`);
      setJobs(res.data.content);
      setTotal(res.data.totalElements);
      setTotalPages(res.data.totalPages);
      setPage(p);
    } catch { logout(); }
    finally { setLoading(false); }
  };

  
  const [savedJobs, setSavedJobs] = useState<number[]>([]);
  const [coverLetter, setCoverLetter] = useState('');
  const [coverLetterJob, setCoverLetterJob] = useState<any>(null);
  const [coverLetterLoading, setCoverLetterLoading] = useState(false);

  useEffect(() => {
    API.get('/api/profile/jobs/saved')
      .then(res => setSavedJobs(res.data.map((s: any) => s.job?.id)))
      .catch(() => {});
  }, []);

  const handleSaveJob = async (jobId: number) => {
    try {
      if (savedJobs.includes(jobId)) {
        await API.delete(`/api/profile/jobs/${jobId}/save`);
        setSavedJobs(prev => prev.filter(id => id !== jobId));
      } else {
        await API.post(`/api/profile/jobs/${jobId}/save`);
        setSavedJobs(prev => [...prev, jobId]);
      }
    } catch (e) {
      alert('Failed. Please try again.');
    }
  };

  const handleCoverLetter = async (job: any) => {
    setCoverLetterJob(job);
    setCoverLetter('');
    setCoverLetterLoading(true);
    try {
      const res = await API.post('/api/cover-letter/generate', {
        jobTitle: job.title,
        company: job.company,
        jobDescription: job.description || ''
      });
      setCoverLetter(res.data.coverLetter);
    } catch (e) {
      setCoverLetter('Failed to generate. Please try again.');
    } finally {
      setCoverLetterLoading(false);
    }
  };

  const handleDownloadCoverLetter = () => {
    const html = `<!DOCTYPE html><html><head><style>body{font-family:Arial,sans-serif;max-width:700px;margin:40px auto;font-size:11pt;line-height:1.6;}</style></head><body><pre style="white-space:pre-wrap;font-family:Arial">${coverLetter}</pre></body></html>`;
    const blob = new Blob([html], {type:'text/html'});
    const url = URL.createObjectURL(blob);
    const w = window.open(url, '_blank');
    if (w) { w.onload = () => { setTimeout(() => w.print(), 500); }; }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      let url = keyword.trim()
        ? `/api/jobs/search?keyword=${keyword}&page=0&size=${pageSize}`
        : `/api/jobs?page=0&size=${pageSize}`;
      const res = await API.get(url);
      setJobs(res.data.content);
      setTotal(res.data.totalElements);
      setTotalPages(res.data.totalPages);
      setPage(0);
    } finally { setLoading(false); }
  };

  const applyFilters = async () => {
    setLoading(true);
    try {
      let url = `/api/jobs?page=0&size=${pageSize}`;
      if (filters.jobType !== 'ALL') url = `/api/jobs/filter/type?jobType=${filters.jobType}&page=0&size=${pageSize}`;
      if (filters.experience !== 'ALL') url = `/api/jobs/filter/experience?level=${filters.experience}&page=0&size=${pageSize}`;
      const res = await API.get(url);
      let filtered = res.data.content as Job[];
      if (filters.location) filtered = filtered.filter(j => j.location?.toLowerCase().includes(filters.location.toLowerCase()));
      if (filters.source !== 'ALL') filtered = filtered.filter(j => j.source === filters.source);
      setJobs(filtered);
      setTotal(filtered.length);
      setTotalPages(Math.ceil(filtered.length / pageSize));
      setPage(0);
    } finally { setLoading(false); }
  };

  const resetFilters = () => {
    setFilters({ jobType: 'ALL', experience: 'ALL', source: 'ALL', location: '', minSalary: '' });
    setKeyword('');
    loadJobs(0);
  };

  const logout = () => { localStorage.clear(); navigate('/login'); };
  const levelColor = (l: string) => l === 'SENIOR' ? '#2d6a9f' : l === 'MID' ? '#2e7d32' : '#f57c00';

  return (
    <div style={s.container}>
      <div style={s.navbar}>
        <span style={s.navTitle}>Job Search App</span>
        <div style={s.navRight}>
          <button style={s.profileBtn} onClick={() => navigate('/profile')}>
            <span style={s.avatarSmall}>{user.fullName?.charAt(0)}</span>
            {user.fullName}
            </button>
            <button style={s.logoutBtn} onClick={logout}>Logout</button>
        </div>
    </div>

      <div style={s.content}>
        {/* Search Bar */}
        <form onSubmit={handleSearch} style={s.searchRow}>
          <input style={s.searchInput} value={keyword}
            onChange={e => setKeyword(e.target.value)}
            placeholder="Search by title, company, skill, location..." />
          <button style={s.searchBtn} type="submit">Search</button>
          <button style={s.filterToggle} type="button"
            onClick={() => setShowFilters(!showFilters)}>
            {showFilters ? 'Hide Filters' : 'Filters'}
          </button>
          <button style={s.clearBtn} type="button" onClick={resetFilters}>Reset</button>
        </form>

        {/* Filter Panel */}
        {showFilters && (
          <div style={s.filterPanel}>
            <div style={s.filterGrid}>
              <div style={s.filterGroup}>
                <label style={s.filterLabel}>Job Type</label>
                <select style={s.select}
                  value={filters.jobType}
                  onChange={e => setFilters({...filters, jobType: e.target.value})}>
                  <option value="ALL">All Types</option>
                  <option value="FULL_TIME">Full Time</option>
                  <option value="PART_TIME">Part Time</option>
                  <option value="CONTRACT">Contract</option>
                </select>
              </div>

              <div style={s.filterGroup}>
                <label style={s.filterLabel}>Experience Level</label>
                <select style={s.select}
                  value={filters.experience}
                  onChange={e => setFilters({...filters, experience: e.target.value})}>
                  <option value="ALL">All Levels</option>
                  <option value="ENTRY">Entry Level</option>
                  <option value="MID">Mid Level</option>
                  <option value="SENIOR">Senior Level</option>
                </select>
              </div>

              <div style={s.filterGroup}>
                <label style={s.filterLabel}>Source</label>
                <select style={s.select}
                  value={filters.source}
                  onChange={e => setFilters({...filters, source: e.target.value})}>
                  <option value="ALL">All Sources</option>
                  <option value="Adzuna">Adzuna</option>
                  <option value="LinkedIn">LinkedIn</option>
                  <option value="Indeed">Indeed</option>
                  <option value="Dice">Dice</option>
                </select>
              </div>

              <div style={s.filterGroup}>
                <label style={s.filterLabel}>Location</label>
                <input style={s.select} placeholder="e.g. New York, Remote"
                  value={filters.location}
                  onChange={e => setFilters({...filters, location: e.target.value})} />
              </div>

              <div style={s.filterGroup}>
                <label style={s.filterLabel}>Jobs Per Page</label>
                <select style={s.select}
                  value={pageSize}
                  onChange={e => setPageSize(Number(e.target.value))}>
                  <option value={10}>10 per page</option>
                  <option value={20}>20 per page</option>
                  <option value={30}>30 per page</option>
                  <option value={50}>50 per page</option>
                </select>
              </div>

              <div style={s.filterGroup}>
                <label style={s.filterLabel}>&nbsp;</label>
                <button style={s.applyFilterBtn} onClick={applyFilters}>
                  Apply Filters
                </button>
              </div>
            </div>

            {/* Quick filter chips */}
            <div style={s.chips}>
              <span style={s.chipLabel}>Quick:</span>
              {['Java', 'Spring Boot', 'React', 'Remote', 'Senior', 'Contract', 'AWS', 'Microservices'].map(chip => (
                <button key={chip} style={s.chip}
                  onClick={() => { setKeyword(chip); handleSearch({ preventDefault: () => {} } as any); }}>
                  {chip}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Results Summary */}
        <div style={s.resultRow}>
          <p style={s.resultCount}>
            {loading ? 'Loading...' : `${total} jobs found — Page ${page+1} of ${totalPages}`}
          </p>
          <div style={s.sortRow}>
            <span style={s.filterLabel}>Sort by:</span>
            <select style={{...s.select, width: 'auto', padding: '6px 12px'}}>
              <option>Latest first</option>
              <option>Salary: High to Low</option>
              <option>Company A-Z</option>
            </select>
          </div>
        </div>

        {/* Job Cards Grid */}
        <div style={s.grid}>
          {jobs.map(job => (
            <div key={job.id} style={s.card}>
              <div style={s.cardTop}>
                <div style={{flex: 1}}>
                  <h3 style={s.jobTitle}>{job.title}</h3>
                  <p style={s.company}>{job.company}</p>
                </div>
                <span style={{...s.levelBadge, background: levelColor(job.experienceLevel)}}>
                  {job.experienceLevel}
                </span>
              </div>

              <div style={s.cardMeta}>
                <span>📍 {job.location || 'USA'}</span>
                {job.salary && <span>💰 {job.salary}</span>}
                <span style={s.sourceTag}>{job.source}</span>
                <span style={{
                  ...s.typeTag,
                  background: job.jobType === 'CONTRACT' ? '#fff3e0' : '#e8f5e9',
                  color: job.jobType === 'CONTRACT' ? '#e65100' : '#2e7d32'
                }}>{job.jobType?.replace('_', ' ')}</span>
              </div>

              <p style={s.description}>{job.description?.substring(0, 140)}...</p>

              <div style={s.skills}>
                {job.skills?.slice(0, 5).map(skill => (
                  <span key={skill} style={s.skill}>{skill}</span>
                ))}
              </div>

              <div style={s.cardActions}>
                <button style={s.aiBtn} onClick={() => setAiJob(job)}>🤖 AI Tailor</button>
                <button style={s.coverBtn} onClick={() => handleCoverLetter(job)}>
                  Cover Letter
                </button>
                <button style={s.applyBtn}
                  onClick={() => job.sourceUrl ? window.open(job.sourceUrl, '_blank') : null}>
                  Apply Now
                </button>
                <button style={savedJobs.includes(job.id) ? s.savedJobBtn : s.saveBtn}
    onClick={() => handleSaveJob(job.id)}>
    {savedJobs.includes(job.id) ? '❤️ Saved' : '🤍 Save Job'}
  </button>
              </div>
            </div>
          ))}
        </div>

        {/* Pagination */}
        <div style={s.pagination}>
          <button style={page === 0 ? s.pageDisabled : s.pageBtn}
            disabled={page === 0} onClick={() => loadJobs(page - 1)}>
            Previous
          </button>
          {Array.from({length: Math.min(5, totalPages)}, (_, i) => {
            const p = Math.max(0, page - 2) + i;
            if (p >= totalPages) return null;
            return (
              <button key={p}
                style={p === page ? s.pageActive : s.pageBtn}
                onClick={() => loadJobs(p)}>
                {p + 1}
              </button>
            );
          })}
          <button style={page >= totalPages-1 ? s.pageDisabled : s.pageBtn}
            disabled={page >= totalPages-1} onClick={() => loadJobs(page + 1)}>
            Next
          </button>
        </div>
      </div>
      {coverLetterJob && (
        <div style={s.overlay}>
          <div style={s.modal}>
            <div style={s.modalHeader}>
              <div>
                <h2 style={s.modalTitle}>Cover Letter</h2>
                <p style={s.modalSub}>{coverLetterJob.title} at {coverLetterJob.company}</p>
              </div>
              <button style={s.closeBtn} onClick={() => setCoverLetterJob(null)}>X</button>
            </div>
            <div style={{padding:24}}>
              {coverLetterLoading ? (
                <div style={{textAlign:'center',padding:'40px 0'}}>
                  <p>Generating your cover letter...</p>
                </div>
              ) : (
                <div>
                  <textarea style={{width:'100%',height:320,padding:12,borderRadius:8,border:'1.5px solid #ddd',fontSize:13,fontFamily:'inherit',resize:'vertical',boxSizing:'border-box'}}
                    value={coverLetter} onChange={e => setCoverLetter(e.target.value)} />
                  <div style={{display:'flex',gap:12,marginTop:12}}>
                    <button style={{flex:1,padding:12,background:'#2e7d32',color:'white',border:'none',borderRadius:8,fontSize:14,fontWeight:600,cursor:'pointer'}}
                      onClick={handleDownloadCoverLetter}>Download PDF</button>
                    <button style={{padding:'12px 16px',background:'white',color:'#2d6a9f',border:'1.5px solid #2d6a9f',borderRadius:8,fontSize:13,cursor:'pointer'}}
                      onClick={() => setCoverLetterJob(null)}>Close</button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
      {aiJob && (
      <AIResumeTailor
          jobTitle={aiJob.title}
          company={aiJob.company}
          jobDescription={aiJob.description}
          onClose={() => setAiJob(null)}
        />
      )}
    </div>
  );
}

const s: { [key: string]: React.CSSProperties } = {
  container: { minHeight: '100vh', background: '#f0f4f8' },
  navbar: { background: '#1e3a5f', color: 'white', padding: '0 32px', height: 60,
    display: 'flex', alignItems: 'center', justifyContent: 'space-between' },
  navTitle: { fontSize: 20, fontWeight: 700 },
  navRight: { display: 'flex', alignItems: 'center', gap: 16 },
  navUser: { fontSize: 14, opacity: 0.9 },
  logoutBtn: { background: 'transparent', border: '1px solid white', color: 'white',
    padding: '6px 16px', borderRadius: 6, cursor: 'pointer', fontSize: 13 },
  content: { maxWidth: 1300, margin: '0 auto', padding: '24px 16px' },
  searchRow: { display: 'flex', gap: 8, marginBottom: 12 },
  searchInput: { flex: 1, padding: '12px 16px', borderRadius: 8,
    border: '1.5px solid #ddd', fontSize: 15, outline: 'none' },
  searchBtn: { padding: '12px 24px', background: '#2d6a9f', color: 'white',
    border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600 },
  filterToggle: { padding: '12px 20px', background: '#f0f4f8', color: '#2d6a9f',
    border: '1.5px solid #2d6a9f', borderRadius: 8, cursor: 'pointer', fontWeight: 600 },
  clearBtn: { padding: '12px 20px', background: '#eee', color: '#333',
    border: 'none', borderRadius: 8, cursor: 'pointer' },
  filterPanel: { background: 'white', borderRadius: 12, padding: 20,
    marginBottom: 16, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' },
  filterGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
    gap: 12, marginBottom: 16 },
  filterGroup: { display: 'flex', flexDirection: 'column', gap: 6 },
  filterLabel: { fontSize: 12, fontWeight: 600, color: '#555', textTransform: 'uppercase', letterSpacing: '0.05em' },
  select: { padding: '8px 12px', borderRadius: 6, border: '1.5px solid #ddd',
    fontSize: 14, outline: 'none', background: 'white', width: '100%' },
  applyFilterBtn: { padding: '8px 16px', background: '#2d6a9f', color: 'white',
    border: 'none', borderRadius: 6, cursor: 'pointer', fontWeight: 600, fontSize: 14 },
  chips: { display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center' },
  chipLabel: { fontSize: 12, color: '#888', fontWeight: 600 },
  chip: { padding: '4px 12px', background: '#e8f0fe', color: '#2d6a9f',
    border: '1px solid #c5d8f8', borderRadius: 20, cursor: 'pointer', fontSize: 13 },
  resultRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  resultCount: { color: '#666', fontSize: 14, margin: 0 },
  sortRow: { display: 'flex', alignItems: 'center', gap: 8 },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 20 },
  card: { background: 'white', borderRadius: 12, padding: 20,
    boxShadow: '0 2px 12px rgba(0,0,0,0.07)', display: 'flex', flexDirection: 'column', gap: 10 },
  cardTop: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8 },
  jobTitle: { fontSize: 16, fontWeight: 700, color: '#1e3a5f', margin: 0 },
  company: { fontSize: 13, color: '#555', margin: '3px 0 0' },
  levelBadge: { color: 'white', fontSize: 11, fontWeight: 600,
    padding: '3px 10px', borderRadius: 20, whiteSpace: 'nowrap' },
  cardMeta: { display: 'flex', gap: 8, fontSize: 12, color: '#555', flexWrap: 'wrap', alignItems: 'center' },
  sourceTag: { background: '#f0f4f8', padding: '2px 8px', borderRadius: 4, fontSize: 11 },
  typeTag: { padding: '2px 8px', borderRadius: 4, fontSize: 11, fontWeight: 600 },
  description: { fontSize: 13, color: '#666', lineHeight: 1.5, margin: 0 },
  skills: { display: 'flex', flexWrap: 'wrap', gap: 5 },
  skill: { background: '#e8f0fe', color: '#2d6a9f', padding: '3px 8px',
    borderRadius: 12, fontSize: 11, fontWeight: 500 },
  cardActions: { display: 'flex', gap: 8, marginTop: 4 },
  aiBtn: { flex: 1, padding: '9px', background: 'white', color: '#2d6a9f', border: '1.5px solid #2d6a9f', borderRadius: 8, cursor: 'pointer', fontWeight: 600, fontSize: 13 },
  applyBtn: { flex: 1, padding: '9px', background: '#2d6a9f', color: 'white',
    border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600, fontSize: 13 },
  savedJobBtn: { flex:1, padding:'9px', background:'#ffebee', color:'#c62828', border:'1.5px solid #c62828', borderRadius:8, cursor:'pointer', fontWeight:600, fontSize:13 },
  overlay: { position:'fixed', inset:0, background:'rgba(0,0,0,0.6)', zIndex:1000, display:'flex', alignItems:'center', justifyContent:'center', padding:16 },
  modal: { background:'white', borderRadius:16, width:'100%', maxWidth:640, maxHeight:'90vh', overflow:'auto', boxShadow:'0 20px 60px rgba(0,0,0,0.3)' },
  modalHeader: { padding:'20px 24px 16px', borderBottom:'1px solid #eee', display:'flex', justifyContent:'space-between', alignItems:'flex-start' },
  modalTitle: { fontSize:20, fontWeight:700, margin:0, color:'#1e3a5f' },
  modalSub: { fontSize:13, color:'#666', margin:'3px 0 0' },
  closeBtn: { background:'none', border:'none', fontSize:20, cursor:'pointer', color:'#666' },
  coverBtn: { flex:1, padding:'9px', background:'#f0f4ff', color:'#2d6a9f', border:'1.5px solid #c5d8f8', borderRadius:8, cursor:'pointer', fontWeight:600, fontSize:13 },
  saveBtn: { padding: '9px 16px', background: 'white', color: '#2d6a9f',
    border: '1.5px solid #2d6a9f', borderRadius: 8, cursor: 'pointer', fontWeight: 600, fontSize: 13 },
  pagination: { display: 'flex', justifyContent: 'center', alignItems: 'center',
    gap: 8, marginTop: 32, marginBottom: 32 },
  pageBtn: { padding: '8px 16px', background: 'white', color: '#2d6a9f',
    border: '1.5px solid #2d6a9f', borderRadius: 6, cursor: 'pointer', fontWeight: 600 },
  pageActive: { padding: '8px 16px', background: '#2d6a9f', color: 'white',
    border: '1.5px solid #2d6a9f', borderRadius: 6, cursor: 'pointer', fontWeight: 600 },
  pageDisabled: { padding: '8px 16px', background: '#eee', color: '#aaa',
    border: '1.5px solid #eee', borderRadius: 6, cursor: 'not-allowed', fontWeight: 600 },
  profileBtn: { background: 'rgba(255,255,255,0.15)', border: '1px solid rgba(255,255,255,0.3)',
    color: 'white', padding: '6px 16px', borderRadius: 20, cursor: 'pointer',
    fontSize: 14, display: 'flex', alignItems: 'center', gap: 8 },
  avatarSmall: { width: 28, height: 28, borderRadius: '50%', background: '#fff',
    color: '#1e3a5f', display: 'inline-flex', alignItems: 'center',
  justifyContent: 'center', fontWeight: 700, fontSize: 13 },
};
