document.getElementById('y').textContent=new Date().getFullYear();
var lines=['<span class="c">// .Jota&Jota</span>','<span class="k">const</span> projeto = <span class="k">await</span> Jota.criar({','  erp: <span class="s">"Sankhya"</span>,','  ia: <span class="s">"integrada"</span>,','  qualidade: <span class="s">"código limpo"</span>,','  prazo: <span class="s">"no tempo certo"</span>','});','','<span class="k">await</span> projeto.deploy();','<span class="c">// ✔ solução no ar</span>'];
var el=document.getElementById('t'),html='',li=0,ci=0;
function plain(s){return s.replace(/<[^>]+>/g,'')}
function type(){
  if(li>=lines.length){el.innerHTML=html+'<span class="caret"></span>';return}
  var src=lines[li],txt=plain(src);
  ci++;
  if(ci>txt.length){html+=src+'\n';li++;ci=0;el.innerHTML=html+'<span class="caret"></span>';return setTimeout(type,220)}
  var shown=0,out='',tag=false;
  for(var i=0;i<src.length&&shown<ci;i++){var ch=src[i];out+=ch;if(ch==='<')tag=true;else if(ch==='>')tag=false;else if(!tag)shown++}
  var open=out.match(/<span[^>]*>/g)||[],close=out.match(/<\/span>/g)||[];
  for(var k=0;k<open.length-close.length;k++)out+='</span>';
  el.innerHTML=html+out+'<span class="caret"></span>';
  setTimeout(type,28);
}
type();
var io=new IntersectionObserver(function(es){es.forEach(function(x){if(x.isIntersecting){x.target.classList.add('on');
x.target.querySelectorAll('[data-n]').forEach(function(b){var t=+b.dataset.n,v=0,st=Math.max(1,Math.round(t/40));var h=setInterval(function(){v+=st;if(v>=t){v=t;clearInterval(h)}b.textContent=v+(t===50?'+':'')},30)});
io.unobserve(x.target)}})},{threshold:.15});
document.querySelectorAll('.rv').forEach(function(e){io.observe(e)});

// Contato: POST para a função serverless. O destino fica no servidor (CONTACT_TO).
(function(){
  var f=document.getElementById('f');if(!f)return;
  var st=document.getElementById('st'),sb=document.getElementById('sb'),t0=Date.now();
  f.addEventListener('submit',function(ev){
    ev.preventDefault();
    var nome=document.getElementById('n').value.trim(),
        email=document.getElementById('e').value.trim(),
        mensagem=document.getElementById('m').value.trim();
    if(nome.length<2||!/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email)||mensagem.length<10){
      st.className='status err';st.textContent='Preencha nome, e-mail válido e uma mensagem com pelo menos 10 caracteres.';return;
    }
    sb.disabled=true;st.className='status';st.textContent='Enviando…';
    fetch('/api/contact',{method:'POST',headers:{'Content-Type':'application/json'},
      body:JSON.stringify({nome:nome,email:email,mensagem:mensagem,website:document.getElementById('hp').value,t:Date.now()-t0})})
    .then(function(r){return r.json().catch(function(){return{ok:r.ok}}).then(function(d){return{r:r,d:d}})})
    .then(function(x){
      if(x.r.ok&&x.d.ok){f.reset();st.className='status ok';st.textContent='Mensagem enviada. Respondemos em breve!';}
      else{st.className='status err';st.textContent=x.d.error||'Não foi possível enviar agora.';}
    })
    .catch(function(){st.className='status err';st.textContent='Falha de conexão. Tente novamente.';})
    .then(function(){sb.disabled=false;});
  });
})();
