// Teste de sintaxe JavaScript extraído do JSP
const labelsProv = [];
const receitasProv = [];
const despesasProv = [];
const provDataMap = {};

// Simulando dados que viriam do JSTL
provDataMap["2024-12-01"] = {
  receita: 1000.50,
  despesa: 0
};

provDataMap["2024-12-15"] = {
  receita: 0,
  despesa: 500.25
};

// Converter mapa para arrays ordenados
Object.keys(provDataMap).sort().forEach(function(dataKey) {
  var partes = dataKey.split('-');
  var label = partes[2] + '/' + partes[1];
  labelsProv.push(label);
  receitasProv.push(provDataMap[dataKey].receita);
  despesasProv.push(provDataMap[dataKey].despesa);
});

console.log('Labels:', labelsProv);
console.log('Receitas:', receitasProv);
console.log('Despesas:', despesasProv);










