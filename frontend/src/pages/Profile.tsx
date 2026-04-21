import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const API = axios.create({ baseURL: 'https://job-search-backend.proudtree-37f0d902.northeurope.azurecontainerapps.io' });
API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

interface Profile {
  headline: string; phone: string; location: string;
  linkedinUrl: string; githubUrl: string; portfolioUrl: string;
  summary: string; skills: string; experience: string;
  education: string; certifications: string; currentTitle: string;
  currentCompany: string; yearsOfExperience: number;
  preferredJobType: string; preferredLocation: string;
  expectedSalary: number; openToRemote: boolean;
  openToRelocation: boolean; resumeFileName: string; resumeText: string;
}

const empty: Profile = {
  headline:'', phone:'', location:'', linkedinUrl:'', githubUrl:'',
  portfolioUrl:'', summary:'', skills:'', experience:'', education:'',
  certifications:'', currentTitle:'', currentCompany:'',
  yearsOfExperience:0, preferredJobType:'', preferredLocation:'',
  expectedSalary:0, openToRemote:true, openToRelocation:false,
  resumeFileName:'', resumeText:''
};

function pct(p: Partial<Profile>) {
  const fields = ['headline','phone','location','summary','skills','experience','education','currentTitle','resumeText'];
  const filled = fields.filter(f => (p as any)[f] && String((p as any)[f]).trim().length > 0);
  return Math.round((filled.length / fields.length) * 100);
}

export default function ProfilePage() {
  const [profile, setProfile] = useState<Profile>(empty);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [activeTab, setActiveTab] = useState('basic');
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const score = pct(profile);

  useEffect(() => {
    API.get('/api/profile')
      .then(res => setProfile({...empty, ...res.data}))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleSave = async () => {
    setSaving(true);
    try {
      await API.put('/api/profile', profile);
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch { alert('Save failed. Please try again.'); }
    finally { setSaving(false); }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    setProfile(p => ({...p, resumeFileName: 'Uploading ' + file.name + '...'}));
    const formData = new FormData();
    formData.append('file', file);
    try {
      const token = localStorage.getItem('token');
      const res = await API.post('/api/resume/upload', formData, {
        headers: { 
          'Content-Type': 'multipart/form-data',
          'Authorization': 'Bearer ' + token
        }
      });
      setProfile(p => ({...p, resumeFileName: file.name, resumeText: res.data.preview}));
      alert('Resume uploaded! ' + res.data.textLength + ' characters extracted from ' + file.name);
    } catch (err: any) {
      setProfile(p => ({...p, resumeFileName: ''}));
      alert(err.response?.data?.error || 'Upload failed. Please paste your resume text below.');
    } finally { setUploading(false); }
  };

  const u = (field: keyof Profile, value: any) =>
    setProfile(p => ({...p, [field]: value}));

  const tabs = [
    {id:'basic', label:'Basic Info', icon:'👤'},
    {id:'experience', label:'Experience', icon:'💼'},
    {id:'skills', label:'Skills', icon:'🛠'},
    {id:'preferences', label:'Job Preferences', icon:'🎯'},
    {id:'resume', label:'Resume', icon:'📄'},
  ];

  if (loading) return (
    <div style={{display:'flex',alignItems:'center',justifyContent:'center',height:'100vh'}}>
      <p>Loading profile...</p>
    </div>
  );

  return (
    <div style={s.container}>
      <div style={s.navbar}>
        <span style={s.navTitle}>Job Search App</span>
        <div style={s.navRight}>
          <button style={s.navBtn} onClick={() => navigate('/jobs')}>Jobs</button>
          <button style={s.navBtnActive}>Profile</button>
          <button style={s.navBtn} onClick={() => { localStorage.clear(); navigate('/login'); }}>Logout</button>
        </div>
      </div>

      <div style={s.content}>
        <div style={s.header}>
          <div style={s.avatar}>{user.fullName?.charAt(0)?.toUpperCase() || 'U'}</div>
          <div style={{flex:1}}>
            <h1 style={s.name}>{user.fullName}</h1>
            <p style={s.emailText}>{user.email}</p>
            <p style={profile.headline ? s.headlineText : s.headlinePlaceholder}>
              {profile.headline || 'Click Basic Info tab to add your headline'}
            </p>
            {profile.location && <p style={s.locationText}>📍 {profile.location}</p>}
          </div>
          <div style={s.headerRight}>
            <div style={s.progressWrap}>
              <svg viewBox="0 0 36 36" style={{width:64,height:64}}>
                <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                  fill="none" stroke="#eee" strokeWidth="3"/>
                <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                  fill="none" stroke="#2d6a9f" strokeWidth="3"
                  strokeDasharray={`${score}, 100`} strokeLinecap="round"/>
                <text x="18" y="20.35" style={{fontSize:'8px',fontWeight:'bold',fill:'#1e3a5f',textAnchor:'middle'}}>
                  {score}%
                </text>
              </svg>
              <span style={s.progressLabel}>Complete</span>
            </div>
            <button style={saved ? s.savedBtn : s.saveBtn} onClick={handleSave} disabled={saving}>
              {saving ? 'Saving...' : saved ? '✅ Saved!' : 'Save Profile'}
            </button>
          </div>
        </div>

        {score < 100 && (
          <div style={s.tipBox}>
            <strong>Complete your profile for better job matches! </strong>
            {!profile.headline && <span style={s.tip}>• Add headline </span>}
            {!profile.skills && <span style={s.tip}>• Add skills </span>}
            {!profile.experience && <span style={s.tip}>• Add experience </span>}
            {!profile.resumeText && <span style={s.tip}>• Upload resume </span>}
          </div>
        )}

        <div style={s.tabs}>
          {tabs.map(tab => (
            <button key={tab.id}
              style={activeTab === tab.id ? s.tabActive : s.tab}
              onClick={() => setActiveTab(tab.id)}>
              {tab.icon} {tab.label}
            </button>
          ))}
        </div>

        <div style={s.card}>

          {activeTab === 'basic' && (
            <div>
              <h2 style={s.sectionTitle}>Basic Information</h2>
              <p style={s.sectionDesc}>This information helps employers find you and our AI match you with the right jobs.</p>
              <div style={s.grid2}>
                <div style={s.field}>
                  <label style={s.label}>Full Name</label>
                  <input style={s.input} value={user.fullName} disabled />
                </div>
                <div style={s.field}>
                  <label style={s.label}>Email Address</label>
                  <input style={s.input} value={user.email} disabled />
                </div>
                <div style={s.field}>
                  <label style={s.label}>Professional Headline *</label>
                  <p style={s.hint}>e.g. Sr. Java Full Stack Developer | 2 Years Experience</p>
                  <input style={s.input} placeholder="e.g. Sr. Java Full Stack Developer"
                    value={profile.headline} onChange={e => u('headline', e.target.value)} />
                </div>
                <div style={s.field}>
                  <label style={s.label}>Current Job Title *</label>
                  <p style={s.hint}>Your current or most recent role</p>
                  <input style={s.input} placeholder="e.g. Software Engineer"
                    value={profile.currentTitle} onChange={e => u('currentTitle', e.target.value)} />
                </div>
                <div style={s.field}>
                  <label style={s.label}>Current Company</label>
                  <input style={s.input} placeholder="e.g. McKinsey and Company"
                    value={profile.currentCompany} onChange={e => u('currentCompany', e.target.value)} />
                </div>
                <div style={s.field}>
                  <label style={s.label}>Phone Number</label>
                  <input style={s.input} placeholder="e.g. +1 660 541 1976"
                    value={profile.phone} onChange={e => u('phone', e.target.value)} />
                </div>
                <div style={s.field}>
                  <label style={s.label}>Location *</label>
                  <input style={s.input} placeholder="e.g. Sunnyvale, CA"
                    value={profile.location} onChange={e => u('location', e.target.value)} />
                </div>
                <div style={s.field}>
                  <label style={s.label}>Years of Experience</label>
                  <input style={s.input} type="number" placeholder="e.g. 2"
                    value={profile.yearsOfExperience || ''}
                    onChange={e => u('yearsOfExperience', Number(e.target.value))} />
                </div>
                <div style={s.field}>
                  <label style={s.label}>LinkedIn URL</label>
                  <input style={s.input} placeholder="linkedin.com/in/your-name"
                    value={profile.linkedinUrl} onChange={e => u('linkedinUrl', e.target.value)} />
                </div>
                <div style={s.field}>
                  <label style={s.label}>GitHub URL</label>
                  <input style={s.input} placeholder="github.com/your-username"
                    value={profile.githubUrl} onChange={e => u('githubUrl', e.target.value)} />
                </div>
                <div style={s.field}>
                  <label style={s.label}>Portfolio Website</label>
                  <input style={s.input} placeholder="e.g. yourname.github.io"
                    value={profile.portfolioUrl} onChange={e => u('portfolioUrl', e.target.value)} />
                </div>
              </div>
              <div style={s.field}>
                <label style={s.label}>Professional Summary *</label>
                <p style={s.hint}>2-4 sentences about your background and expertise</p>
                <textarea style={s.textarea} rows={4}
                  placeholder="e.g. Software Engineer with 2+ years experience building scalable Java microservices..."
                  value={profile.summary} onChange={e => u('summary', e.target.value)} />
              </div>
            </div>
          )}

          {activeTab === 'experience' && (
            <div>
              <h2 style={s.sectionTitle}>Work Experience & Education</h2>
              <p style={s.sectionDesc}>Add your work history. AI uses this to tailor your resume for each job.</p>
              <div style={s.field}>
                <label style={s.label}>Work Experience *</label>
                <p style={s.hint}>List each job — company, title, dates, responsibilities</p>
                <textarea style={s.textarea} rows={10}
                  placeholder={'Company | Job Title | Start - End Date\n• Responsibility 1\n• Responsibility 2\n\nExample:\nMcKinsey & Company | Software Engineer | Dec 2023 - Present\n• Built Java 17 microservices with Spring Boot\n• Deployed on AWS EKS with Docker and Kubernetes'}
                  value={profile.experience} onChange={e => u('experience', e.target.value)} />
              </div>
              <div style={s.field}>
                <label style={s.label}>Education</label>
                <textarea style={s.textarea} rows={3}
                  placeholder="e.g. M.S. Computer Science | Northwest Missouri State | 2022-2023"
                  value={profile.education} onChange={e => u('education', e.target.value)} />
              </div>
              <div style={s.field}>
                <label style={s.label}>Certifications</label>
                <textarea style={s.textarea} rows={2}
                  placeholder="e.g. AWS Certified Developer, Spring Professional"
                  value={profile.certifications} onChange={e => u('certifications', e.target.value)} />
              </div>
            </div>
          )}

          {activeTab === 'skills' && (
            <div>
              <h2 style={s.sectionTitle}>Technical Skills</h2>
              <p style={s.sectionDesc}>Add all your skills separated by commas. Used to match you with the right jobs.</p>
              <div style={s.field}>
                <label style={s.label}>Your Skills *</label>
                <p style={s.hint}>Separate each skill with a comma</p>
                <textarea style={s.textarea} rows={5}
                  placeholder="e.g. Java, Spring Boot, React, TypeScript, AWS, Docker, Kubernetes, Kafka"
                  value={profile.skills} onChange={e => u('skills', e.target.value)} />
              </div>
              {profile.skills && profile.skills.trim() && (
                <div style={{marginTop:16}}>
                  <label style={s.label}>Skills Preview:</label>
                  <div style={s.skillChips}>
                    {profile.skills.split(',').filter(sk => sk.trim()).map((skill, i) => (
                      <span key={i} style={s.skillChip}>{skill.trim()}</span>
                    ))}
                  </div>
                </div>
              )}
              <div style={s.skillSuggest}>
                <label style={s.label}>Click to add common skills:</label>
                <div style={s.skillChips}>
                  {['Java','Spring Boot','React','TypeScript','Python','AWS','Azure','Docker',
                    'Kubernetes','Kafka','PostgreSQL','MongoDB','Redis','Microservices','REST APIs',
                    'Git','Jenkins','GraphQL'].map(skill => (
                    <button key={skill} style={s.addChip}
                      onClick={() => {
                        const cur = profile.skills ? profile.skills.split(',').map(s => s.trim()) : [];
                        if (!cur.includes(skill)) u('skills', [...cur, skill].join(', '));
                      }}>+ {skill}</button>
                  ))}
                </div>
              </div>
            </div>
          )}

          {activeTab === 'preferences' && (
            <div>
              <h2 style={s.sectionTitle}>Job Preferences</h2>
              <p style={s.sectionDesc}>Tell us what kind of jobs you are looking for.</p>
              <div style={s.grid2}>
                <div style={s.field}>
                  <label style={s.label}>Preferred Job Type</label>
                  <select style={s.input} value={profile.preferredJobType}
                    onChange={e => u('preferredJobType', e.target.value)}>
                    <option value="">-- Select --</option>
                    <option value="FULL_TIME">Full Time (W2)</option>
                    <option value="CONTRACT">Contract (C2C / 1099)</option>
                    <option value="PART_TIME">Part Time</option>
                    <option value="ANY">Open to Any</option>
                  </select>
                </div>
                <div style={s.field}>
                  <label style={s.label}>Preferred Location</label>
                  <input style={s.input} placeholder="e.g. Remote, Sunnyvale CA"
                    value={profile.preferredLocation} onChange={e => u('preferredLocation', e.target.value)} />
                </div>
                <div style={s.field}>
                  <label style={s.label}>Expected Salary (Annual $)</label>
                  <input style={s.input} type="number" placeholder="e.g. 150000"
                    value={profile.expectedSalary || ''}
                    onChange={e => u('expectedSalary', Number(e.target.value))} />
                  {profile.expectedSalary > 0 && (
                    <span style={{fontSize:12,color:'#2d6a9f',marginTop:4}}>
                      ${profile.expectedSalary.toLocaleString()} / year
                    </span>
                  )}
                </div>
                <div style={s.field}>
                  <label style={s.label}>Open to Remote?</label>
                  <div style={s.toggleRow}>
                    <button style={profile.openToRemote ? s.toggleOn : s.toggleOff}
                      onClick={() => u('openToRemote', true)}>Yes, Remote</button>
                    <button style={!profile.openToRemote ? s.toggleOn : s.toggleOff}
                      onClick={() => u('openToRemote', false)}>Office Only</button>
                  </div>
                </div>
                <div style={s.field}>
                  <label style={s.label}>Open to Relocation?</label>
                  <div style={s.toggleRow}>
                    <button style={profile.openToRelocation ? s.toggleOn : s.toggleOff}
                      onClick={() => u('openToRelocation', true)}>Yes</button>
                    <button style={!profile.openToRelocation ? s.toggleOn : s.toggleOff}
                      onClick={() => u('openToRelocation', false)}>No</button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'resume' && (
            <div>
              <h2 style={s.sectionTitle}>Your Resume</h2>
              <p style={s.sectionDesc}>
                Upload your resume in ANY format — PDF, DOCX, DOC, TXT, RTF.
                Our AI will extract the text and use it to tailor your resume for each job.
              </p>

              <div style={s.resumeBox}>
                <div style={{fontSize:48,marginBottom:12}}>📄</div>
                <h3 style={{fontSize:18,fontWeight:700,color:'#1e3a5f',margin:'0 0 8px'}}>
                  {uploading ? '⏳ Uploading...' :
                   profile.resumeFileName ? '✅ ' + profile.resumeFileName :
                   'No resume uploaded yet'}
                </h3>
                <p style={{fontSize:14,color:'#666',marginBottom:20}}>
                  Supported: PDF, DOCX, DOC, TXT, RTF and any text format
                </p>
                <input type="file" accept="*/*"
                  style={{display:'none'}} id="resumeInput"
                  onChange={handleFileUpload} />
                <label htmlFor="resumeInput" style={uploading ? s.uploadBtnDisabled : s.uploadBtn}>
                  {uploading ? 'Uploading...' :
                   profile.resumeFileName ? '🔄 Replace Resume' : '📤 Upload Resume'}
                </label>
              </div>

              <div style={s.field}>
                <label style={s.label}>Or paste your resume text here</label>
                <p style={s.hint}>Copy and paste the full text of your resume</p>
                <textarea style={s.textarea} rows={14}
                  placeholder="Paste your complete resume text here..."
                  value={profile.resumeText || ''}
                  onChange={e => u('resumeText', e.target.value)} />
              </div>

              <div style={s.aiBox}>
                <h3 style={{fontSize:18,fontWeight:700,color:'#1e3a5f',margin:'0 0 8px'}}>
                  🤖 AI Resume Tailor
                </h3>
                <p style={{fontSize:14,color:'#555',lineHeight:1.7,marginBottom:16}}>
                  Save your resume here then click <strong>AI Tailor</strong> on any job to get your ATS score and an optimized version!
                </p>
                <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:8,marginBottom:16}}>
                  {['Match job keywords','Boost ATS score','Highlight experience','Tailor summary','Optimize skills','Download ready'].map((f,i) => (
                    <span key={i} style={{fontSize:13,color:'#2d6a9f',fontWeight:500}}>✅ {f}</span>
                  ))}
                </div>
                <div style={s.aiWarning}>⚡ Coming soon: One-click apply with auto-filled forms!</div>
              </div>
            </div>
          )}

        </div>

        <div style={{display:'flex',justifyContent:'flex-end',gap:12,marginTop:8,paddingBottom:32}}>
          <button style={s.cancelBtn} onClick={() => navigate('/jobs')}>Back to Jobs</button>
          <button style={saved ? s.savedBtn : s.saveBtn} onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : saved ? '✅ Profile Saved!' : 'Save Profile'}
          </button>
        </div>
      </div>
    </div>
  );
}

function Field({label, hint, children}: {label:string; hint?:string; children:React.ReactNode}) {
  return (
    <div style={{marginBottom:20}}>
      <label style={{display:'block',fontSize:12,fontWeight:700,color:'#333',
        textTransform:'uppercase',letterSpacing:'0.05em',marginBottom:4}}>{label}</label>
      {hint && <p style={{fontSize:12,color:'#888',margin:'0 0 6px'}}>{hint}</p>}
      {children}
    </div>
  );
}

const s: {[key:string]:React.CSSProperties} = {
  container:{minHeight:'100vh',background:'#f0f4f8'},
  navbar:{background:'#1e3a5f',color:'white',padding:'0 32px',height:60,
    display:'flex',alignItems:'center',justifyContent:'space-between'},
  navTitle:{fontSize:20,fontWeight:700},
  navRight:{display:'flex',gap:8,alignItems:'center'},
  navBtn:{background:'transparent',border:'1px solid rgba(255,255,255,0.3)',
    color:'white',padding:'6px 16px',borderRadius:6,cursor:'pointer',fontSize:13},
  navBtnActive:{background:'rgba(255,255,255,0.2)',border:'1px solid white',
    color:'white',padding:'6px 16px',borderRadius:6,cursor:'pointer',fontSize:13,fontWeight:600},
  content:{maxWidth:900,margin:'0 auto',padding:'32px 16px'},
  header:{background:'white',borderRadius:16,padding:28,marginBottom:16,
    display:'flex',alignItems:'center',gap:20,boxShadow:'0 2px 12px rgba(0,0,0,0.08)'},
  avatar:{width:72,height:72,borderRadius:'50%',background:'#2d6a9f',color:'white',
    display:'flex',alignItems:'center',justifyContent:'center',fontSize:28,fontWeight:700,flexShrink:0},
  name:{fontSize:22,fontWeight:700,color:'#1e3a5f',margin:0},
  emailText:{fontSize:14,color:'#888',margin:'3px 0'},
  headlineText:{fontSize:14,color:'#555',margin:'3px 0 0'},
  headlinePlaceholder:{fontSize:14,color:'#aaa',margin:'3px 0 0',fontStyle:'italic'},
  locationText:{fontSize:13,color:'#888',margin:'3px 0 0'},
  headerRight:{marginLeft:'auto',display:'flex',flexDirection:'column',alignItems:'center',gap:8},
  progressWrap:{display:'flex',flexDirection:'column',alignItems:'center',gap:4},
  progressLabel:{fontSize:11,color:'#888'},
  tipBox:{background:'#fff8e1',border:'1px solid #ffe082',borderRadius:10,
    padding:'12px 16px',marginBottom:16,fontSize:13},
  tip:{color:'#f57c00',fontWeight:500,marginLeft:8},
  tabs:{display:'flex',gap:4,marginBottom:16,flexWrap:'wrap'},
  tab:{padding:'10px 16px',background:'white',border:'0.5px solid #ddd',
    borderRadius:8,cursor:'pointer',fontSize:13,color:'#555'},
  tabActive:{padding:'10px 16px',background:'#2d6a9f',border:'0.5px solid #2d6a9f',
    borderRadius:8,cursor:'pointer',fontSize:13,color:'white',fontWeight:600},
  card:{background:'white',borderRadius:16,padding:28,
    boxShadow:'0 2px 12px rgba(0,0,0,0.08)',marginBottom:16},
  sectionTitle:{fontSize:20,fontWeight:700,color:'#1e3a5f',margin:'0 0 6px'},
  sectionDesc:{fontSize:14,color:'#666',marginBottom:24},
  grid2:{display:'grid',gridTemplateColumns:'1fr 1fr',gap:16,marginBottom:8},
  field:{display:'flex',flexDirection:'column',gap:4,marginBottom:16},
  label:{fontSize:12,fontWeight:700,color:'#333',textTransform:'uppercase',letterSpacing:'0.04em'},
  hint:{fontSize:12,color:'#888',margin:0},
  input:{padding:'10px 14px',borderRadius:8,border:'1.5px solid #ddd',
    fontSize:14,outline:'none',background:'white'},
  textarea:{padding:'10px 14px',borderRadius:8,border:'1.5px solid #ddd',
    fontSize:14,outline:'none',resize:'vertical',fontFamily:'inherit'},
  skillChips:{display:'flex',flexWrap:'wrap',gap:8,marginTop:8},
  skillChip:{background:'#e8f0fe',color:'#2d6a9f',padding:'6px 14px',
    borderRadius:20,fontSize:13,fontWeight:500},
  skillSuggest:{marginTop:24,padding:16,background:'#f8f9ff',borderRadius:10,border:'1px solid #e8f0fe'},
  addChip:{padding:'5px 12px',background:'white',color:'#2d6a9f',
    border:'1.5px solid #c5d8f8',borderRadius:20,cursor:'pointer',fontSize:12,fontWeight:500},
  toggleRow:{display:'flex',gap:8,marginTop:4},
  toggleOn:{flex:1,padding:'10px',background:'#2d6a9f',color:'white',
    border:'none',borderRadius:8,cursor:'pointer',fontWeight:600,fontSize:13},
  toggleOff:{flex:1,padding:'10px',background:'white',color:'#555',
    border:'1.5px solid #ddd',borderRadius:8,cursor:'pointer',fontSize:13},
  resumeBox:{background:'#f8f9ff',borderRadius:12,padding:28,textAlign:'center',
    border:'2px dashed #c5d8f8',marginBottom:24},
  uploadBtn:{display:'inline-block',padding:'12px 28px',background:'#2d6a9f',
    color:'white',borderRadius:8,cursor:'pointer',fontWeight:600,fontSize:14},
  uploadBtnDisabled:{display:'inline-block',padding:'12px 28px',background:'#90a4ae',
    color:'white',borderRadius:8,cursor:'not-allowed',fontWeight:600,fontSize:14},
  aiBox:{background:'#f0f4ff',borderRadius:12,padding:24,border:'1px solid #c5d8f8',marginTop:16},
  aiWarning:{background:'#fff3e0',padding:'10px 16px',borderRadius:8,
    fontSize:13,color:'#e65100',fontWeight:600,marginTop:12},
  saveBtn:{padding:'12px 32px',background:'#2d6a9f',color:'white',
    border:'none',borderRadius:8,cursor:'pointer',fontWeight:600,fontSize:15},
  savedBtn:{padding:'12px 32px',background:'#2e7d32',color:'white',
    border:'none',borderRadius:8,cursor:'pointer',fontWeight:600,fontSize:15},
  cancelBtn:{padding:'12px 24px',background:'white',color:'#555',
    border:'1.5px solid #ddd',borderRadius:8,cursor:'pointer',fontSize:14},
};
