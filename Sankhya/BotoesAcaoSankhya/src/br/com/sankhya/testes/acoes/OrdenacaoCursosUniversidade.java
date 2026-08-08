package br.com.sankhya.testes.acoes;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import com.sankhya.util.ReflectUtils;
import com.sankhya.util.ReflectUtils.KeyBuilder;
import com.sankhya.util.StringUtils;

public class OrdenacaoCursosUniversidade implements AcaoRotinaJava{

	public void doAction(ContextoAcao contexto) throws Exception{
		BigDecimal codAluno = (BigDecimal) contexto.getLinhaPai().getCampo("ID");
		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = null;
		try {
			jdbc = EntityFacade.getJdbcWrapperFromOtherDataSource("MySQLDB");
			NativeSql query = new NativeSql(jdbc);
			query.appendSql("select * from course_categories order by parent, sortorder");
			ResultSet rs = query.executeQuery();
			Map<Long, Categoria> cats = new HashMap<Long, Categoria>();
			while(rs.next()){
				Categoria cat= new Categoria(rs.getLong("id"), rs.getLong("parent"), rs.getLong("sortorder"));
				cats.put(cat.id, cat);
			}
			rs.close();
			organize(cats);

			query = new NativeSql(jdbc);
			query.appendSql(" SELECT CUR.ID, CUR.CATEGORY, CUR.SORTORDER FROM ");
			query.appendSql(" 	COURSE_USER CUSER ");
			query.appendSql("   INNER JOIN COURSE CUR ON CUR.ID = CUSER.COURSEID ");
			query.appendSql("   INNER JOIN COURSE_CATEGORIES CCAT ON CCAT.ID = CUR.CATEGORY ");
			query.appendSql(" WHERE ");
			query.appendSql("   CUSER.USERID = :ALUNO ");
			query.setNamedParameter("ALUNO", codAluno);
			rs = query.executeQuery();

			ArrayList<Curso> cursos = new ArrayList<Curso>();
			while(rs.next()){
				Categoria cat = cats.get(rs.getLong("CATEGORY"));
				cursos.add(new Curso(rs.getLong("ID"), cat.strOrder, rs.getLong("SORTORDER")));
			}
			rs.close();
			Collections.sort(cursos, new Comparator<Curso>() {
				public int compare(Curso c0, Curso c1) {
					int comp = c0.categoryOrder.compareTo(c1.categoryOrder);
					if(comp == 0){
						comp = c0.courseOrder.compareTo(c1.courseOrder);
					}
					return comp;
				}
			});

			Collection<PersistentLocalEntity> registros = (Collection<PersistentLocalEntity>) dwfFacade.findByDynamicFinder(new FinderWrapper("CursoAlunoUniversidade", "this.USERID = ?", new Object [] {codAluno}));

			Map<Long, PersistentLocalEntity> cursosAluno = new HashMap<Long, PersistentLocalEntity>();
			cursosAluno = ReflectUtils.buildMapFromCollection(registros, new KeyBuilder() {
				public Object build(Object item) {
					try{
						return ((DynamicVO) ((PersistentLocalEntity) item).getValueObject()).asBigDecimal("COURSEID").longValue();
					}catch (Exception e) {
						RuntimeException rte = new RuntimeException();
						rte.initCause(e);
						throw rte;
					}
				}
			});

			int i = 1;
			for(Curso c:cursos){
				PersistentLocalEntity curso = cursosAluno.get(c.id);
				DynamicVO voCurso = (DynamicVO) curso.getValueObject();
				voCurso.setProperty("SORTORDER", new BigDecimal(i++));
				curso.setValueObject((EntityVO) voCurso);
			}
		}finally{
			JdbcWrapper.closeSession(jdbc);
		}

	}

	private static void organize(Map<Long, Categoria> cats){
		List<Categoria> base = new ArrayList<Categoria>();
		for(Categoria cat:cats.values()){
			Categoria parent = cats.get(cat.parentID);
			if(parent == null){
				base.add(cat);
			} else {
				cat.setParent(parent);
			}
		}
		Collections.sort(base, new Comparator<Categoria>() {
			public int compare(Categoria o1, Categoria o2) {
				return o1.order.compareTo(o2.order);
			}
		});

		int i = 1;
		for(Categoria cat:base){
			cat.strOrder = StringUtils.stringZero(i++, 5);
			cat.sort();

		}
	}

	private static class Curso{

		Long id;
		String categoryOrder;
		Long courseOrder;

		private Curso(Long id, String categoryOrder, Long courseOrder){
			this.id = id;
			this.categoryOrder = categoryOrder;
			this.courseOrder = courseOrder;
		}
	}

	private static class Categoria{

		private Long id;
		private Long parentID;
		private Long order;
		private String strOrder;
		private Categoria parent;
		private List<Categoria> filhos = new ArrayList<Categoria>();

		private Categoria(Long id, Long parentID, Long order){
			this.id = id;
			this.parentID = parentID;
			this.order = order;
		}

		private void setParent(Categoria parent){
			this.parent = parent;
			this.parent.filhos.add(this);
		}

		private void sort(){
			Collections.sort(filhos, new Comparator<Categoria>() {
				public int compare(Categoria o1, Categoria o2) {
					return o1.order.compareTo(o2.order);
				}
			});
			int i = 1;
			for(Categoria f:filhos){
				f.strOrder = strOrder + StringUtils.stringZero(i++, 5);
				f.sort();
			}
		}
	}

}
