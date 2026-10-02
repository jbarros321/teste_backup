/* ============================================================
   CONTEÚDO DO CURRÍCULO — bilíngue (pt-BR / en).
   Edite somente este arquivo.
   Para traduzir algo, mude o texto nas DUAS chaves: pt e en.
   ============================================================ */

/* Fatos que não mudam com o idioma. */
export const profile = {
  name: "Jonatan Barros de Jesus",
  initials: "JB",
  email: "jbarros213@icloud.com",
  linkedin: "https://www.linkedin.com/in/jonatan-barros-93a044213",
  linkedinLabel: "shre.ink/G17c",
  github: "",            // TODO
};

export const LANGS = ["pt", "en"];
export const DEFAULT_LANG = "pt";

export const content = {
  /* ══════════════════════ PORTUGUÊS ══════════════════════ */
  pt: {
    htmlLang: "pt-BR",
    label: "PT",
    switchTo: "Mudar para inglês",
    docTitle: "Jonatan Barros de Jesus — Tech Lead & Soluções ERP",
    docDesc:
      "Tech Lead, consultor SQL e desenvolvedor de soluções ERP em Uberlândia, MG. Soluções ERP escaláveis, rotinas SQL, automação com Java.",

    nav: [
      { href: "#about",   label: "Sobre" },
      { href: "#work",    label: "Experiência" },
      { href: "#skills",  label: "Competências" },
      { href: "#contact", label: "Contato" },
    ],

    availability: "Disponível · Uberlândia, MG, Brasil",
    firstName: "Jonatan",
    lastName: "Barros de Jesus",
    scrollCue: "scroll",

    roles: ["Tech Lead", "Consultor SQL", "Desenvolvedor HTML & Java", "Soluções ERP"],

    metrics: [
      { value: 8, suffix: "+", label: "anos de carreira" },
      { value: 2, suffix: "",  label: "empresas, uma trajetória" },
      { value: 4, suffix: "",  label: "cargos, cada um uma promoção" },
    ],

    sections: {
      about:    "Resumo",
      work:     "Experiência",
      skills:   "Competências",
      training: "Formação complementar",
    },

    summary: [
      "Minha carreira cresceu do administrativo para a análise de nível de serviço, a gestão de processos financeiros e, hoje, a liderança técnica em desenvolvimento de produtos. Tenho experiência sólida na construção de rotinas SQL, na estruturação de soluções em HTML e na automação de processos com Java.",
      "Como consultor, construo soluções ERP escaláveis que combinam domínio técnico com pensamento analítico para resolver problemas e melhorar a eficiência do negócio — do controle operacional e da conciliação financeira ao desenvolvimento de tecnologia.",
    ],

    nowBadge: "agora",

    companies: [
      {
        company: "Neuon",
        span: "2 anos 8 meses",
        current: true,
        roles: [
          {
            title: "Tech Lead",
            period: "fev. 2026 – o momento",
            meta: "Consultoria e desenvolvimento de produtos",
            bullets: [
              "Liderança técnica de projetos de consultoria e desenvolvimento de produtos.",
              "Desenho de soluções ERP escaláveis que alinham tecnologia à estratégia do negócio.",
            ],
            tags: ["Liderança técnica", "Arquitetura ERP", "Consultoria", "Produto"],
          },
          {
            title: "Desenvolvedor Full-Stack",
            period: "mar. 2024 – mar. 2026",
            meta: "Tempo integral · Remoto",
            bullets: [
              "Desenvolvimento de rotinas SQL em banco de dados.",
              "Estruturação e desenvolvimento de interfaces HTML.",
              "Automação de processos e integração de sistemas com Java.",
            ],
            tags: ["SQL", "HTML", "Java", "Integrações", "Remoto"],
          },
        ],
      },
      {
        company: "Refrigerantes do Triângulo Ltda.",
        span: "6 anos 1 mês",
        roles: [
          {
            title: "Analista de Nível de Serviço",
            period: "abr. 2022 – mar. 2024",
            meta: "Tempo integral · Uberlândia, MG, Brasil",
            bullets: [
              "Análise financeira e gestão de contas a receber.",
              "Controle operacional e conciliação financeira.",
              "Monitoramento de indicadores de nível de serviço e melhoria de processos.",
            ],
            tags: ["Análise financeira", "Contas a receber", "Conciliação", "SLA"],
          },
          {
            title: "Auxiliar Administrativo",
            period: "mar. 2018 – out. 2022",
            meta: "Tempo integral · Uberlândia, MG, Brasil",
            bullets: [
              "Condução de rotinas administrativas e suporte às equipes de operações e financeiro.",
            ],
            tags: ["Rotinas administrativas", "Suporte à operação"],
          },
        ],
      },
    ],

    clusters: [
      {
        title: "Dados & SQL",
        line: "Onde os números têm que fechar.",
        items: [
          "Rotinas SQL em banco de dados",
          "Consultoria especializada em SQL",
          "Modelagem e otimização de consultas",
          "Lógica de conciliação financeira",
        ],
      },
      {
        title: "Soluções ERP",
        line: "Sistemas escaláveis que acompanham como o negócio realmente funciona.",
        items: [
          "Soluções e consultoria ERP",
          "Controle operacional",
          "Da regra de negócio à tela entregue",
          "Tecnologia alinhada à estratégia",
        ],
      },
      {
        title: "Desenvolvimento",
        line: "Da interface até a integração.",
        items: [
          "Desenvolvimento e estruturação em HTML",
          "Java para automação e integração",
          "Desenvolvimento full-stack",
          "Integração de sistemas",
        ],
      },
      {
        title: "Liderança & Análise",
        line: "A parte que não é código.",
        items: [
          "Liderança técnica",
          "Desenvolvimento de produtos",
          "Análise de nível de serviço",
          "Identificação e resolução de problemas",
        ],
      },
    ],

    stack: [
      "SQL", "ERP", "Java", "HTML", "JavaScript", "Full-Stack",
      "Rotinas de banco", "Integração de sistemas", "Automação de processos",
      "Análise financeira", "Contas a receber", "Conciliação",
      "Análise de nível de serviço", "Liderança técnica",
    ],

    training: [
      { name: "JavaScript", issuer: "DIO — Digital Innovation One" },
      { name: "Java",       issuer: "DIO — Digital Innovation One" },
    ],

    contact: {
      kicker: "Vamos conversar",
      big: "Tem um processo que ainda<br><em>roda em planilha?</em>",
      keys: { email: "E-mail", linkedin: "LinkedIn", location: "Localização" },
      location: "Uberlândia, MG, Brasil",
    },
  },

  /* ══════════════════════ ENGLISH ══════════════════════ */
  en: {
    htmlLang: "en",
    label: "EN",
    switchTo: "Switch to Portuguese",
    docTitle: "Jonatan Barros de Jesus — Tech Lead & ERP Solutions",
    docDesc:
      "Tech Lead, SQL consultant and ERP solutions developer based in Uberlândia, Brazil. Scalable ERP solutions, SQL routines, Java automation.",

    nav: [
      { href: "#about",   label: "About" },
      { href: "#work",    label: "Experience" },
      { href: "#skills",  label: "Capabilities" },
      { href: "#contact", label: "Contact" },
    ],

    availability: "Available · Uberlândia, MG, Brazil",
    firstName: "Jonatan",
    lastName: "Barros de Jesus",
    scrollCue: "scroll",

    roles: ["Tech Lead", "SQL Consultant", "HTML & Java Developer", "ERP Solutions"],

    metrics: [
      { value: 8, suffix: "+", label: "years in the field" },
      { value: 2, suffix: "",  label: "companies, one trajectory" },
      { value: 4, suffix: "",  label: "roles, each a promotion" },
    ],

    sections: {
      about:    "Summary",
      work:     "Experience",
      skills:   "Capabilities",
      training: "Additional training",
    },

    summary: [
      "My career has grown from administrative work into service level analysis, financial process management and, today, technical leadership in product development. I have solid experience building SQL routines, structuring solutions in HTML and automating processes with Java.",
      "As a consultant, I build scalable ERP solutions that combine technical expertise with analytical thinking to solve problems and improve business efficiency — from operational control and financial reconciliation to technology development.",
    ],

    nowBadge: "now",

    companies: [
      {
        company: "Neuon",
        span: "2 yrs 8 mos",
        current: true,
        roles: [
          {
            title: "Tech Lead",
            period: "Feb 2026 – Present",
            meta: "Consulting and product development",
            bullets: [
              "Technical leadership of consulting and product development projects.",
              "Design of scalable ERP solutions that align technology with business strategy.",
            ],
            tags: ["Technical leadership", "ERP architecture", "Consulting", "Product"],
          },
          {
            title: "Full-Stack Developer",
            period: "Mar 2024 – Mar 2026",
            meta: "Full-time · Remote",
            bullets: [
              "Development of SQL database routines.",
              "Structuring and development of HTML interfaces.",
              "Process automation and systems integration with Java.",
            ],
            tags: ["SQL", "HTML", "Java", "Integrations", "Remote"],
          },
        ],
      },
      {
        company: "Refrigerantes do Triângulo Ltda.",
        span: "6 yrs 1 mo",
        roles: [
          {
            title: "Service Level Analyst",
            period: "Apr 2022 – Mar 2024",
            meta: "Full-time · Uberlândia, MG, Brazil",
            bullets: [
              "Financial analysis and accounts receivable management.",
              "Operational control and financial reconciliation.",
              "Monitoring of service level indicators and process improvement.",
            ],
            tags: ["Financial analysis", "Accounts receivable", "Reconciliation", "SLA"],
          },
          {
            title: "Administrative Assistant",
            period: "Mar 2018 – Oct 2022",
            meta: "Full-time · Uberlândia, MG, Brazil",
            bullets: [
              "Handled administrative routines and supported the operations and finance teams.",
            ],
            tags: ["Administrative routines", "Operations support"],
          },
        ],
      },
    ],

    clusters: [
      {
        title: "Data & SQL",
        line: "Where the numbers have to add up.",
        items: [
          "SQL database routines",
          "Specialized SQL consulting",
          "Query modelling and optimisation",
          "Financial reconciliation logic",
        ],
      },
      {
        title: "ERP Solutions",
        line: "Scalable systems that match how the business actually runs.",
        items: [
          "ERP solutions and consulting",
          "Operational control",
          "Business rules to delivered screens",
          "Technology aligned with strategy",
        ],
      },
      {
        title: "Development",
        line: "From the interface down to the integration.",
        items: [
          "HTML development and structuring",
          "Java for automation and integration",
          "Full-stack development",
          "Systems integration",
        ],
      },
      {
        title: "Leadership & Analysis",
        line: "The part that isn't code.",
        items: [
          "Technical leadership",
          "Product development",
          "Service level analysis",
          "Problem identification and resolution",
        ],
      },
    ],

    stack: [
      "SQL", "ERP", "Java", "HTML", "JavaScript", "Full-Stack",
      "Database Routines", "Systems Integration", "Process Automation",
      "Financial Analysis", "Accounts Receivable", "Reconciliation",
      "Service Level Analysis", "Technical Leadership",
    ],

    training: [
      { name: "JavaScript", issuer: "DIO — Digital Innovation One" },
      { name: "Java",       issuer: "DIO — Digital Innovation One" },
    ],

    contact: {
      kicker: "Let's talk",
      big: "Got a process that still<br><em>runs on a spreadsheet?</em>",
      keys: { email: "Email", linkedin: "LinkedIn", location: "Location" },
      location: "Uberlândia, MG, Brazil",
    },
  },
};
