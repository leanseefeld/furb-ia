package br.furb.ia.trab1.interpretador;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.furb.ia.trab1.Aula;
import br.furb.ia.trab1.Disciplina;

public class InterpretadorHorario {

	private static final String INSTRUCTION_PREFIX = ":";

	private List<Disciplina> disciplinas;

	private Scanner $sc;
	private String $linePreview;
	private boolean $onPreview;

	private GradeInst gradeInst = new GradeInst();
	private CursadasInst cursadasInst = new CursadasInst();
	private DependenciaInst dependenciaInst = new DependenciaInst();
	private OndeInst ondeInst = new OndeInst();
	private Map<String, Instrucao> instrucoes = new HashMap<>();

	private List<Disciplina> cursadas;

	public InterpretadorHorario(Scanner sc) {
		this.$sc = sc;
		putInst(gradeInst, cursadasInst, dependenciaInst, ondeInst);
	}

	private void putInst(Instrucao... insts) {
		for (Instrucao inst : insts) {
			instrucoes.put(inst.getNomeComando().toLowerCase(), inst);
		}
	}

	public void interpretar() {
		// ler
		String line = null;
		while ((line = nextLine()) != null) {
			if (!isEmpty(line)) {
				Instrucao inst = null;
				if (line.startsWith(INSTRUCTION_PREFIX)) {
					String token = line.substring(INSTRUCTION_PREFIX.length());
					inst = instrucoes.get(token.toLowerCase());
				}
				if (inst == null) {
					throw new InputMismatchException(String.format("Esperava um comando, mas encontrou \"%s\"", isEmpty(line) ? "<vazio>" : line));
				}
				inst.processar();
			}
		}

		// associar
		Map<String, Disciplina> disciplinas = new HashMap<>();
		for (GradeString gradeString : gradeInst.getGrades()) {
			disciplinas.putAll(gradeString.asDisciplinas());
		}
		dependenciaInst.inserirDependencias(disciplinas);
		cursadas = cursadasInst.selecionarCursadas(disciplinas);

		// deve ser o último passo
		ondeInst.atualizaDefinicoes(disciplinas);

		this.disciplinas = new LinkedList<>(disciplinas.values());
	}

	public List<Disciplina> getDisciplinas() {
		return disciplinas;
	}

	public List<Disciplina> getCursadas() {
		return cursadas;
	}

	private Integer tryInteger() {
		String preview = previewNextLine();
		Integer value = null;
		try {
			value = Integer.parseInt(preview);
			nextLine();
		} catch (NumberFormatException nfe) {
		}
		return value;
	}

	private String nextLine() {
		try {
			if ($onPreview) {
				return $linePreview;
			}
			return $sc.hasNextLine() ? $sc.nextLine() : null;
		} finally {
			$onPreview = false;
		}
	}

	private String previewNextLine() {
		if ($onPreview) {
			return $linePreview;
		}
		$onPreview = true;
		return $linePreview = $sc.hasNextLine() ? $sc.nextLine() : null;
	}

	private String[] requireTokens(String tokenPattern, int tokenMinCount) {
		String line = nextLine();
		String[] tokens = new String[tokenMinCount];

		Matcher m = Pattern.compile(tokenPattern).matcher(line);
		for (int i = 0; i < tokenMinCount; i++) {
			if (!m.find()) {
				throw new InputMismatchException(String.format("not enough matches for pattern \"%s\" on the line \"%s\"", tokenPattern, line));
			}
			tokens[i] = m.group();
		}
		return tokens;
	}

	public static boolean isEmpty(String str) {
		return str == null || "".equals(str.trim());
	}

	// Instruções

	private static abstract class Instrucao {

		abstract String getNomeComando();

		abstract void processar();

	}

	private class GradeInst extends Instrucao {

		private Map<Integer, GradeString> grades = new HashMap<>();

		@Override
		String getNomeComando() {
			return "grade";
		}

		@Override
		void processar() {
			Integer semestre;
			while ((semestre = tryInteger()) != null) {
				String[] tokens1 = requireTokens("\\S+", 5);
				String[] tokens2 = requireTokens("\\S+", 5);
				GradeString grade = new GradeString(semestre, //
						new String[][] { tokens1, tokens2 });
				if (grades.put(semestre, grade) != null) {
					throw new InputMismatchException(String.format("Grade do semestre %d já declarada", semestre.intValue()));
				}
			}
			if (grades.isEmpty()) {
				throw new InputMismatchException("Informe a grade de ao menos um semestre");
			}
		}

		public Collection<GradeString> getGrades() {
			return grades.values();
		}

	}

	private class DependenciaInst extends Instrucao {

		private static final String TOKEN_DEPENDS = "->";
		private Map<String, String> dependencias = new HashMap<>();

		@Override
		String getNomeComando() {
			return "dependencia";
		}

		public void inserirDependencias(Map<String, Disciplina> disciplinas) {
			for (Map.Entry<String, String> dependencia : dependencias.entrySet()) {
				String origem = dependencia.getKey();
				Disciplina disciplina = disciplinas.get(origem);
				if (disciplina == null) {
					System.err.println("Disciplina não delacarada não pode ter dependência: " + origem);
				} else {
					String destino = dependencia.getValue();
					Disciplina disciplinaDependencia = disciplinas.get(destino);
					if (disciplinaDependencia == null) {
						System.err.println("Dependência ausente na grade: " + destino);
					} else {
						disciplina.setPreRequisito(disciplinaDependencia);
					}
				}
			}
		}

		@Override
		void processar() {
			String exp = null;
			while (!isEmpty(exp = previewNextLine())) {
				if (!exp.contains(TOKEN_DEPENDS)) {
					System.err.println("Expressão de dependência não reconhecida: " + exp);
					break;
				}
				nextLine();
				String[] tokens = exp.split(TOKEN_DEPENDS);
				if (tokens.length < 2) {
					throw new InputMismatchException("O operador \"->\" exige pelo menos duas disciplinas");
				}
				String lastToken = null;
				for (String token : tokens) {
					if (lastToken != null) {
						dependencias.put(lastToken.trim(), token.trim());
					}
					lastToken = token;
				}
			}
		}

	}

	private class CursadasInst extends Instrucao {

		private List<String> cursadasStr = new LinkedList<>();

		@Override
		String getNomeComando() {
			return "cursadas";
		}

		public List<Disciplina> selecionarCursadas(Map<String, Disciplina> disciplinas) {
			List<Disciplina> disciplinasCursadas = new LinkedList<>();
			for (String cursadaStr : cursadasStr) {
				Disciplina disciplina = disciplinas.get(cursadaStr);
				if (disciplina == null) {
					System.err.println("Disciplina cursada ausente na grade: " + cursadaStr);
				} else {
					disciplinasCursadas.add(disciplina);
				}
			}
			return disciplinasCursadas;
		}

		@Override
		void processar() {
			String list = nextLine();
			if (!isEmpty(list)) {
				String[] tokens = list.split("\\s+");
				for (String token : tokens) {
					cursadasStr.add(token);
				}
			}
		}

	}

	private class OndeInst extends Instrucao {

		private Map<String, String> definicoes = new HashMap<>();

		@Override
		String getNomeComando() {
			return "onde";
		}

		@Override
		void processar() {
			String line = null;
			while (!isEmpty(line = previewNextLine())) {
				String[] tokens = line.split("\\s?=\\s?", 2);
				if (tokens.length != 2) {
					throw new InputMismatchException("Informe um nome para a disciplina");
				}
				if (definicoes.put(tokens[0], tokens[1]) != null) {
					System.err.println("Expressão de disciplina sobrescrita: " + line);
				}
				nextLine();
			}
		}

		public void atualizaDefinicoes(Map<String, Disciplina> disciplinas) {
			Map<String, Disciplina> toAdd = new HashMap<>();
			for (Map.Entry<String, String> definicao : definicoes.entrySet()) {
				String antigoNome = definicao.getKey();
				Disciplina disciplina = disciplinas.get(antigoNome);
				if (disciplina == null) {
					System.err.println("Disciplina não declarada: " + antigoNome);
				} else {
					String novoNome = definicao.getValue();
					disciplina.setNome(novoNome);
					toAdd.put(novoNome, disciplina);
				}
			}
			for (String disciplina : definicoes.keySet()) {
				disciplinas.remove(disciplina);
			}
			disciplinas.putAll(toAdd);
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		try (Scanner sc = new Scanner(new File("res/Grade01.grd"))) {
			InterpretadorHorario interpretador = new InterpretadorHorario(sc);
			interpretador.interpretar();

			List<Disciplina> disciplinasLidas = interpretador.getDisciplinas();
			System.out.println("Disciplinas:\n" + disciplinasLidas);
			System.out.println("\nDisciplinas cursadas:\n" + interpretador.getCursadas());

			System.out.println("\nAulas:");
			for (Disciplina disciplina : disciplinasLidas) {
				System.out.println(disciplina.getSemestre() + " - " + disciplina.getNome());
				for (Aula aula : disciplina.getAulas()) {
					System.out.println("\t" + aula.getDia() + "/" + aula.getHorario());
				}
			}

			System.out.println("\nDisciplinas:");
			for (Disciplina disciplina : disciplinasLidas) {
				if (disciplina.getPreRequisito() != null) {
					System.out.println(disciplina.getNome() + " -> " + disciplina.getPreRequisito().getNome());
				}
			}

		}
	}

}
