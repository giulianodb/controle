package service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import util.NumeroUtil;
import control.SociosAplication;
import control.exception.ExceptionHandler;
import dto.participacao.DetalhesParticipacaoAdvogadoDTO;
import dto.participacao.ItemParticipacaoFaturaDTO;
import dto.participacao.ItemParticipacaoUsuarioDTO;
import dto.participacao.ParticipacaoFaturaDTO;
import dto.participacao.ParticipacaoUsuarioDTO;
import entity.Caso;
import entity.Cliente;
import entity.Conta;
import entity.Fatura;
import entity.Participacao;
import entity.PreParticipacaoExito;
import entity.PreParticipacaoFixo;
import entity.Prolabore;
import entity.StatusTrabalho;
import entity.TipoTransacaoEnum;
import entity.Trabalho;
import entity.Transacao;
import entity.Usuario;
import entity.ValorParticipacaoEnum;
import exception.ControleException;

@Stateless
@LocalBean
@ExceptionHandler
public class ParticipacaoService {
	@PersistenceContext(unitName = "controle")
	private EntityManager em;

	@Inject
	private SociosAplication sociosAplication;

	@EJB
	private ParticipacaoService participacaoService;
	@EJB
	private ParticipacaoDefaultService participacaoDefaultService;
	@EJB
	private TransacaoService transacaoService;

	@EJB
	private ContaService contaService;

	@EJB
	private ClienteService clienteService;

	@EJB
	private UsuarioService usuarioService;

	@EJB
	private FaturaService faturaService;

	private List<Usuario> listaUsuarioCombo;

	private List<Cliente> listaClienteCombo;

	private List<Caso> listaCasoCombo;

	/*
	 * Cadastra participação.
	 * 
	 * A forma como foi tratada foi assim: A idéia e ter uma mapara ir
	 * adicionando participações. As participações são referente a uma fatura,
	 * porém os valores são encontrados nos trabalhos.
	 * 
	 * É realizado uma iteração nos trabalhos e a participação de cada trabalho
	 * é feita da forma que: Primeiro é obtida a lista de sócios, esta lista é
	 * iterada para criar a particição referente aos sócios (sócios tem direito
	 * a todos) depois verifica se esse socio iterado não faz parte da indicação
	 * ou do advogado que fez o trabalho. Caso for encontrado já popula as
	 * informa.ções
	 */

	public void cadastrarParticipacao(Fatura fatura) {

		Map<Integer, Participacao> mapaParticipacao = new HashMap<Integer, Participacao>();

		Float percentagemImposto = fatura.getPorcentagemImposto();

		// Esse recurso muito avançada foi criado para resolver o problema de
		// exm. 100/3 = 33 porém 1 real fica vagando pelo espaço tempo..

		Float valorCriterioTrabalho = 0f;
		Float valorCriterioSociedade = 0f;
		Float valorCriterioImposto = 0f;
		Float valorCriterioFundo = 0f;
		Float valorCriterioIndicacao = 0f;
		Float valorSomaParticipacao = 0f;

		for (Trabalho trabalho : fatura.getListaTrabalhos()) {

			// valor trbalho com redução de imposto
			Float valorTotalTrabalho = obterValorTotalTrabalhoSemImposto(
					trabalho.getValorTotalTrabalho(), percentagemImposto);
			// Float valorTotalTrabalho =
			// (NumeroUtil.multiplicarDinheiro(trabalho.getValorTotalTrabalho(),(100
			// - percentagemImposto)/100));
			// lista de usuarios compartilhamento
			List<Usuario> listaUsuariosCompartilhamento = trabalho
					.getUsuarios();

			Float valorTrabalhoCompartilhado;
			Integer quantidadeCompartilhamento = 0;
			Boolean temCompartilhamento = false;
			Boolean diferencasValores = false;

			if (listaUsuariosCompartilhamento.size() > 0) {
				temCompartilhamento = true;
				quantidadeCompartilhamento = listaUsuariosCompartilhamento
						.size() + 1;
			}

			List<Usuario> listaIndicados = trabalho.getCaso()
					.getListaUsuarioIndicacao();
			Participacao participacao;

			if (listaIndicados == null || listaIndicados.size() == 0) {
				throw new ControleException(
						"Alguns casos não possuem indicação.");
			}

			for (Usuario usuario : sociosAplication.getListaSocios()) {
				participacao = new Participacao();
				float valorParticipacao = 0;

				participacao.setAdvogado(usuario);
				// inves da porcentagem, seta o valor totoal de sócio

				// valorParticipacao =
				// NumeroUtil.multiplicarDinheiro(NumeroUtil.multiplicarDinheiro(NumeroUtil.DividirDinheiro(fatura.getPorcentagemParticipacaoSocio(),
				// 100f), NumeroUtil.DividirDinheiro(usuario.getParticipacao() ,
				// 100f)), valorTotalTrabalho);

				valorParticipacao = obterValorParticipacaoSocio(fatura,
						usuario, valorTotalTrabalho);
				participacao.setPorcentagemParticipacaoSocio(valorParticipacao);

				if (listaIndicados.contains(usuario)) {
					// divide a percentagem por 100. multiplica o valor
					// disponivel para indicacao pelo valor percentagem do
					// advogado

					valorParticipacao = obterValorParticipacaoIndicacao(
							valorParticipacao, fatura, listaIndicados.size(),
							valorTotalTrabalho);
					// valorParticipacao =
					// NumeroUtil.somarDinheiro(valorParticipacao,
					// NumeroUtil.multiplicarDinheiro(NumeroUtil.DividirDinheiro(fatura.getPorcentagemIndicacao(),
					// 100f),
					// (NumeroUtil.multiplicarDinheiro(NumeroUtil.DividirDinheiro(100f,
					// NumeroUtil.DividirDinheiro(Float.valueOf(listaIndicados.size()),
					// 100f)) , valorTotalTrabalho) )));
					//
					participacao
							.setPorcentagemIndicacao(obterPorcentagemIndicacao(
									fatura, listaIndicados.size(),
									valorTotalTrabalho));

				}
				// adiciona participação caso advogado for o mesmo do trabalho.
				if (usuario.equals(trabalho.getAdvogado())) {
					valorParticipacao = obterValorParticipacaoTrabalho(
							fatura.getPorcentagemParticipacaoTrabalho(),
							valorTotalTrabalho, valorParticipacao,
							temCompartilhamento, quantidadeCompartilhamento);
					// valorParticipacao =
					// NumeroUtil.somarDinheiro(valorParticipacao,
					// NumeroUtil.multiplicarDinheiro(NumeroUtil.DividirDinheiro(fatura.getPorcentagemParticipacaoTrabalho(),
					// 100f), valorTotalTrabalho));

					participacao
							.setPorcentagemParticipacaoTrabalho(obterPorcentagemParticipacaoTrabalho(
									fatura, valorTotalTrabalho,
									temCompartilhamento,
									quantidadeCompartilhamento));
				}

				// repassar pela lista de compartilhamentos, caso ouver
				if (temCompartilhamento) {
					if (listaUsuariosCompartilhamento.contains(usuario)) {
						valorParticipacao = obterValorParticipacaoTrabalho(
								fatura.getPorcentagemParticipacaoTrabalho(),
								valorTotalTrabalho, valorParticipacao,
								temCompartilhamento, quantidadeCompartilhamento);
						// valorParticipacao =
						// NumeroUtil.somarDinheiro(valorParticipacao,
						// NumeroUtil.multiplicarDinheiro(NumeroUtil.DividirDinheiro(fatura.getPorcentagemParticipacaoTrabalho(),
						// 100f), valorTotalTrabalho));

						participacao
								.setPorcentagemParticipacaoTrabalho(obterPorcentagemParticipacaoTrabalho(
										fatura, valorTotalTrabalho,
										temCompartilhamento,
										quantidadeCompartilhamento));
					}
				}

				participacao.setFatura(fatura);

				participacao.setValorTotalParticipacao(valorParticipacao);

				participacao.setValorTotalPago(0f);
				participacao.setValorDisponivel(0f);

				// Inserir participacação no mapa e caso já estiver advogado com
				// participação realiaza atualização dos valores
				if (mapaParticipacao.get(participacao.getAdvogado().getIdUsuario()) != null) {
					float valorAtual = mapaParticipacao.get(participacao.getAdvogado().getIdUsuario()).getValorTotalParticipacao();
					float valorAdicional = participacao.getValorTotalParticipacao();
					float valorSocioAtual = mapaParticipacao.get(participacao.getAdvogado().getIdUsuario()).getPorcentagemParticipacaoSocio();
					float valorSocioAdicional = participacao.getPorcentagemParticipacaoSocio();
					float valorTrabalhoAtual = mapaParticipacao.get(participacao.getAdvogado().getIdUsuario()).getPorcentagemParticipacaoTrabalho();
					float valorTrabalhoAdicional = participacao.getPorcentagemParticipacaoTrabalho();
					float valorIndicacaoAtual = mapaParticipacao.get(participacao.getAdvogado().getIdUsuario()).getPorcentagemIndicacao();
					float valorIndicacaoAdicional = participacao.getPorcentagemIndicacao();

					mapaParticipacao.get(participacao.getAdvogado().getIdUsuario()).setValorTotalParticipacao(NumeroUtil.somarDinheiro(valorAtual,valorAdicional, 2));
					mapaParticipacao.get(participacao.getAdvogado().getIdUsuario()).setPorcentagemIndicacao(NumeroUtil.somarDinheiro(valorIndicacaoAtual,valorIndicacaoAdicional, 2));
					mapaParticipacao.get(participacao.getAdvogado().getIdUsuario()).setPorcentagemParticipacaoSocio(NumeroUtil.somarDinheiro(valorSocioAtual,valorSocioAdicional, 2));
					mapaParticipacao.get(participacao.getAdvogado().getIdUsuario()).setPorcentagemParticipacaoTrabalho(NumeroUtil.somarDinheiro(valorTrabalhoAtual,valorTrabalhoAdicional, 2));

				} else {
					mapaParticipacao.put(participacao.getAdvogado().getIdUsuario(), participacao);
				}

			}
			for (Usuario usuario : listaIndicados) {
				participacao = new Participacao();
				float valorParticipacao = 0;
				// verifica se já não é um dos socios, caso
				if (!sociosAplication.getListaSocios().contains(usuario)) {
					participacao
							.setPorcentagemIndicacao(obterPorcentagemIndicacao(
									fatura, listaIndicados.size(),
									valorTotalTrabalho));
					participacao.setAdvogado(usuario);
					valorParticipacao = obterValorParticipacaoIndicacao(
							valorParticipacao, fatura, listaIndicados.size(),
							valorTotalTrabalho);

					if (usuario.equals(trabalho.getAdvogado())) {
						participacao
								.setPorcentagemParticipacaoTrabalho(obterPorcentagemParticipacaoTrabalho(
										fatura, valorTotalTrabalho,
										temCompartilhamento,
										quantidadeCompartilhamento));
						valorParticipacao = obterValorParticipacaoTrabalho(
								fatura.getPorcentagemParticipacaoTrabalho(),
								valorTotalTrabalho, valorParticipacao,
								temCompartilhamento, quantidadeCompartilhamento);
						participacao.setAdvogado(usuario);
					}

					// repassar pela lista de compartilhamentos, caso ouver
					if (temCompartilhamento) {
						if (listaUsuariosCompartilhamento.contains(usuario)) {
							valorParticipacao = obterValorParticipacaoTrabalho(
									fatura.getPorcentagemParticipacaoTrabalho(),
									valorTotalTrabalho, valorParticipacao,
									temCompartilhamento,
									quantidadeCompartilhamento);
							// valorParticipacao =
							// NumeroUtil.somarDinheiro(valorParticipacao,
							// NumeroUtil.multiplicarDinheiro(NumeroUtil.DividirDinheiro(fatura.getPorcentagemParticipacaoTrabalho(),
							// 100f), valorTotalTrabalho));

							participacao
									.setPorcentagemParticipacaoTrabalho(obterPorcentagemParticipacaoTrabalho(
											fatura, valorTotalTrabalho,
											temCompartilhamento,
											quantidadeCompartilhamento));
						}
					}

					// Só salva se tiver entrando nas condições
					if (participacao.getAdvogado() != null) {
						participacao
								.setValorTotalParticipacao(valorParticipacao);
						participacao.setFatura(fatura);
						participacao.setValorDisponivel(0f);
						participacao.setValorTotalPago(0f);

						// Inserir participacação no mapa e caso já estiver
						// advogado com participação realiaza atualização dos
						// valores
						if (mapaParticipacao.get(participacao.getAdvogado()
								.getIdUsuario()) != null) {
							float valorAtual = mapaParticipacao.get(
									participacao.getAdvogado().getIdUsuario())
									.getValorTotalParticipacao();
							float valorAdicional = participacao
									.getValorTotalParticipacao();
							float valorSocioAtual = mapaParticipacao.get(
									participacao.getAdvogado().getIdUsuario())
									.getPorcentagemParticipacaoSocio();
							float valorSocioAdicional = participacao
									.getPorcentagemParticipacaoSocio();
							float valorTrabalhoAtual = mapaParticipacao.get(
									participacao.getAdvogado().getIdUsuario())
									.getPorcentagemParticipacaoTrabalho();
							float valorTrabalhoAdicional = participacao
									.getPorcentagemParticipacaoTrabalho();
							float valorIndicacaoAtual = mapaParticipacao.get(
									participacao.getAdvogado().getIdUsuario())
									.getPorcentagemIndicacao();
							float valorIndicacaoAdicional = participacao
									.getPorcentagemIndicacao();

							mapaParticipacao.get(
									participacao.getAdvogado().getIdUsuario())
									.setValorTotalParticipacao(
											valorAtual + valorAdicional);
							mapaParticipacao.get(
									participacao.getAdvogado().getIdUsuario())
									.setPorcentagemIndicacao(
											valorIndicacaoAtual
													+ valorIndicacaoAdicional);
							mapaParticipacao.get(
									participacao.getAdvogado().getIdUsuario())
									.setPorcentagemParticipacaoSocio(
											valorSocioAtual
													+ valorSocioAdicional);
							mapaParticipacao.get(
									participacao.getAdvogado().getIdUsuario())
									.setPorcentagemParticipacaoTrabalho(
											valorTrabalhoAtual
													+ valorTrabalhoAdicional);
						} else {
							mapaParticipacao.put(participacao.getAdvogado()
									.getIdUsuario(), participacao);
						}

					}
				}

			}

			for (Usuario usuario : listaUsuariosCompartilhamento) {
				participacao = new Participacao();
				float valorParticipacao = 0;

				participacao
						.setPorcentagemParticipacaoTrabalho(obterPorcentagemParticipacaoTrabalho(
								fatura, valorTotalTrabalho,
								temCompartilhamento, quantidadeCompartilhamento));
				participacao.setAdvogado(usuario);
				participacao.setFatura(fatura);

				participacao
						.setValorTotalParticipacao(obterValorParticipacaoTrabalho(
								fatura.getPorcentagemParticipacaoTrabalho(),
								valorTotalTrabalho, 0f, temCompartilhamento,
								quantidadeCompartilhamento));
				participacao.setValorDisponivel(0f);
				participacao.setValorTotalPago(0f);

				if (!sociosAplication.getListaSocios().contains(
						trabalho.getAdvogado())) {
					if (!listaIndicados.contains(usuario)) {
						// repassar pela lista de compartilhamentos, caso ouver
						if (temCompartilhamento) {
							if (listaUsuariosCompartilhamento.contains(usuario)) {
								valorParticipacao = obterValorParticipacaoTrabalho(
										fatura.getPorcentagemParticipacaoTrabalho(),
										valorTotalTrabalho, valorParticipacao,
										temCompartilhamento,
										quantidadeCompartilhamento);
								// valorParticipacao =
								// NumeroUtil.somarDinheiro(valorParticipacao,
								// NumeroUtil.multiplicarDinheiro(NumeroUtil.DividirDinheiro(fatura.getPorcentagemParticipacaoTrabalho(),
								// 100f), valorTotalTrabalho));

								participacao
										.setPorcentagemParticipacaoTrabalho(obterPorcentagemParticipacaoTrabalho(
												fatura, valorTotalTrabalho,
												temCompartilhamento,
												quantidadeCompartilhamento));
							}
						}
					}
				}

				// Inserir participacação no mapa e caso já estiver advogado com
				// participação realiaza atualização dos valores
				if (mapaParticipacao.get(participacao.getAdvogado()
						.getIdUsuario()) != null) {
					float valorAtual = mapaParticipacao.get(
							participacao.getAdvogado().getIdUsuario())
							.getValorTotalParticipacao();
					float valorAdicional = participacao
							.getValorTotalParticipacao();
					float valorSocioAtual = mapaParticipacao.get(
							participacao.getAdvogado().getIdUsuario())
							.getPorcentagemParticipacaoSocio();
					float valorSocioAdicional = participacao
							.getPorcentagemParticipacaoSocio();
					float valorTrabalhoAtual = mapaParticipacao.get(
							participacao.getAdvogado().getIdUsuario())
							.getPorcentagemParticipacaoTrabalho();
					float valorTrabalhoAdicional = participacao
							.getPorcentagemParticipacaoTrabalho();
					float valorIndicacaoAtual = mapaParticipacao.get(
							participacao.getAdvogado().getIdUsuario())
							.getPorcentagemIndicacao();
					float valorIndicacaoAdicional = participacao
							.getPorcentagemIndicacao();

					mapaParticipacao.get(
							participacao.getAdvogado().getIdUsuario())
							.setValorTotalParticipacao(
									valorAtual + valorAdicional);
					mapaParticipacao.get(
							participacao.getAdvogado().getIdUsuario())
							.setPorcentagemIndicacao(
									valorIndicacaoAtual
											+ valorIndicacaoAdicional);
					mapaParticipacao.get(
							participacao.getAdvogado().getIdUsuario())
							.setPorcentagemParticipacaoSocio(
									valorSocioAtual + valorSocioAdicional);
					mapaParticipacao
							.get(participacao.getAdvogado().getIdUsuario())
							.setPorcentagemParticipacaoTrabalho(
									valorTrabalhoAtual + valorTrabalhoAdicional);
				} else {
					mapaParticipacao.put(participacao.getAdvogado()
							.getIdUsuario(), participacao);
				}

			}

			if (!sociosAplication.getListaSocios().contains(
					trabalho.getAdvogado())) {
				if (!listaIndicados.contains(trabalho.getAdvogado())) {
					participacao = new Participacao();
					participacao
							.setPorcentagemParticipacaoTrabalho(obterPorcentagemParticipacaoTrabalho(
									fatura, valorTotalTrabalho,
									temCompartilhamento,
									quantidadeCompartilhamento));
					participacao.setAdvogado(trabalho.getAdvogado());
					participacao.setFatura(fatura);

					participacao
							.setValorTotalParticipacao(obterValorParticipacaoTrabalho(
									fatura.getPorcentagemParticipacaoTrabalho(),
									valorTotalTrabalho, 0f,
									temCompartilhamento,
									quantidadeCompartilhamento));
					participacao.setValorDisponivel(0f);
					participacao.setValorTotalPago(0f);

					// Inserir participacação no mapa e caso já estiver advogado
					// com participação realiaza atualização dos valores
					if (mapaParticipacao.get(participacao.getAdvogado()
							.getIdUsuario()) != null) {
						float valorAtual = mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.getValorTotalParticipacao();
						float valorAdicional = participacao
								.getValorTotalParticipacao();
						float valorSocioAtual = mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.getPorcentagemParticipacaoSocio();
						float valorSocioAdicional = participacao
								.getPorcentagemParticipacaoSocio();
						float valorTrabalhoAtual = mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.getPorcentagemParticipacaoTrabalho();
						float valorTrabalhoAdicional = participacao
								.getPorcentagemParticipacaoTrabalho();
						float valorIndicacaoAtual = mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.getPorcentagemIndicacao();
						float valorIndicacaoAdicional = participacao
								.getPorcentagemIndicacao();

						mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.setValorTotalParticipacao(
										valorAtual + valorAdicional);
						mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.setPorcentagemIndicacao(
										valorIndicacaoAtual
												+ valorIndicacaoAdicional);
						mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.setPorcentagemParticipacaoSocio(
										valorSocioAtual + valorSocioAdicional);
						mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.setPorcentagemParticipacaoTrabalho(
										valorTrabalhoAtual
												+ valorTrabalhoAdicional);
					} else {
						mapaParticipacao.put(participacao.getAdvogado()
								.getIdUsuario(), participacao);
					}

				}
			}

			// cadastrar imposto

			participacao = new Participacao();
			participacao.setImposto(true);
			participacao.setFatura(fatura);
			participacao.setValorTotalParticipacao(obterValorImposto(fatura,
					trabalho.getValorTotalTrabalho()));

			participacao.setValorDisponivel(0f);
			participacao.setValorTotalPago(0f);

			if (mapaParticipacao.get(-1) != null) {
				float valorAtual = mapaParticipacao.get(-1)
						.getValorTotalParticipacao();
				float valorAdicional = participacao.getValorTotalParticipacao();
				mapaParticipacao.get(-1).setValorTotalParticipacao(
						valorAtual + valorAdicional);
			} else {
				mapaParticipacao.put(-1, participacao);
			}

			// cadastrar fundo

			participacao = new Participacao();
			participacao.setFundo(true);
			participacao.setFatura(fatura);
			participacao.setValorTotalParticipacao(obterValotFundo(fatura,
					valorTotalTrabalho));

			participacao.setValorDisponivel(0f);
			participacao.setValorTotalPago(0f);

			// Inserir participacação no mapa e caso já estiver advogado com
			// participação realiaza atualização dos valores
			if (mapaParticipacao.get(0) != null) {
				float valorAtual = mapaParticipacao.get(0)
						.getValorTotalParticipacao();
				float valorAdicional = participacao.getValorTotalParticipacao();
				mapaParticipacao.get(0).setValorTotalParticipacao(
						valorAtual + valorAdicional);
			} else {
				mapaParticipacao.put(0, participacao);
			}

		}

		// percorre o mapa e realiza a persistência dos dados
		Collection<Participacao> cont = mapaParticipacao.values();
		Iterator<Participacao> i = cont.iterator();
		while (i.hasNext()) {
			if (i != null) {
				Participacao item = i.next();
				valorSomaParticipacao = NumeroUtil.somarDinheiro(
						valorSomaParticipacao,
						item.getValorTotalParticipacao(), 3);

				item.setValorDisponivel(NumeroUtil.deixarFloatDuasCasas(item
						.getValorDisponivel()));
				item.setValorTotalPago(NumeroUtil.deixarFloatDuasCasas(item
						.getValorTotalPago()));
				item.setValorTotalParticipacao(NumeroUtil
						.deixarFloatDuasCasas(item.getValorTotalParticipacao()));

				em.persist(item);
			}
		}

		// verifica se a soma das participações é igual ao valor fatura
		if (!valorSomaParticipacao.equals(fatura.valorTotalFatura())) {
			// Obtem a diferença e insere no fundo
			Float dieferenca = NumeroUtil.diminuirDinheiro( 
					fatura.valorTotalFatura(), valorSomaParticipacao, 3);
			
			
			dieferenca = NumeroUtil.deixarFloatDuasCasas(dieferenca);
			
			if (dieferenca < 2f) {
				Participacao fundo = mapaParticipacao.get(0);
				
				Float valorNovo = NumeroUtil.somarDinheiro(
						fundo.getValorTotalParticipacao(), dieferenca, 3);
				valorNovo = NumeroUtil.deixarFloatDuasCasas(valorNovo);
				
				fundo.setValorTotalParticipacao(valorNovo);
				
				em.persist(fundo);
			} else {
				System.out.println("Diferença é maior que 2: " + dieferenca);
			}
			

		}

	}

	/**
	 * Método usado para salvar as participações inseridas após a geração de
	 * fatura. Usado nas faturas fixas já geradas sem pre participação. Pois
	 * agora serão realizadas as participações.
	 * 
	 * @param f
	 */
	public void salvarParticipacaoPosterior(Prolabore p) {
		for (PreParticipacaoFixo pf : p.getListaPreParticipacaoFixos()) {
//			if (pf.getIdPreParticipacao() == null) {
				
				List<Participacao> listaParticipacao = listarParticipacaoPorFatura(p.getFatura().getIdFatura());
				
				boolean fazContinue= false;
				
				for (Participacao participacao : listaParticipacao) {
					
					if(participacao.getAdvogado() != null && participacao.getAdvogado().getIdUsuario().equals(pf.getAdvogado().getIdUsuario())) {
						float valorPre = 0f;
						if (p.getValorParticipacaoEnum().equals(ValorParticipacaoEnum.VALOR)) {
							valorPre = pf.getValorParticipacao();
						} else {
							valorPre = NumeroUtil.valorDaPorcentagem(p.getValorParticipacaoTrabalho(),pf.getValorParticipacao());
						}

						participacao.setPorcentagemParticipacaoTrabalho(valorPre);
						participacao.setValorTotalParticipacao(NumeroUtil.somarDinheiro(participacao.getValorTotalParticipacao(), valorPre, 4));
						
						em.merge(participacao);
						fazContinue = true;
						break;
					}
				}
				
				if (fazContinue) {
					continue;
				}
				
				
				Participacao participacao = new Participacao();
				participacao.setAdvogado(pf.getAdvogado());
				participacao.setFatura(p.getFatura());
				participacao.setFundo(false);
				participacao.setImposto(false);

				float valorPre = 0f;
				if (p.getValorParticipacaoEnum().equals(ValorParticipacaoEnum.VALOR)) {
					valorPre = pf.getValorParticipacao();
				} else {
					valorPre = NumeroUtil.valorDaPorcentagem(p.getValorParticipacaoTrabalho(),pf.getValorParticipacao());
				}

				participacao.setPorcentagemParticipacaoTrabalho(valorPre);
				participacao.setValorTotalParticipacao(valorPre);

				if (participacao.getFatura().getStatusFatura()
						.equals(StatusTrabalho.PAGO)) {
					participacao.setValorDisponivel(valorPre);
					participacao.setDataPagamentoParticipacao(new Date());

					// Para cada transação devo alterar o valor da conta também.
					Conta conta = new Conta();

					if (participacao.getAdvogado() != null
							&& participacao.getAdvogado().getIdUsuario() != null) {
						conta = contaService.obterContaPorAdvogado(participacao
								.getAdvogado().getIdUsuario());
						Float valorDisponivel = NumeroUtil.somarDinheiro(
								conta.getValorDisponivel(),
								participacao.getValorTotalParticipacao(), 3);

						conta.setValorDisponivel(NumeroUtil
								.deixarFloatDuasCasas(valorDisponivel));

						contaService.alterarConta(conta);
					}

				} else {
					participacao.setValorDisponivel(0f);
				}

				participacao.setValorTotalPago(0f);

				em.persist(participacao);
				
				
				
				
//			}
//			else {
//				pf.getIdPreParticipacao();
//			}
		}

	}
	

	public void cadastrarParticipacaoFixo(Fatura fatura) {

		Map<Integer, Participacao> mapaParticipacao = new HashMap<Integer, Participacao>();

		Float percentagemImposto = fatura.getPorcentagemImposto();

		// Esse recurso muito avançada foi criado para resolver o problema de
		// exm. 100/3 = 33 porém 1 real fica vagando pelo espaço tempo..

		Float valorSomaParticipacao = 0f;

		Prolabore prolabore = fatura.getListaProlabore().get(0);

		Float valorPreParticipacaoSemImposto = participacaoService
				.obterValorTotalTrabalhoSemImposto(prolabore.getValor()
						.floatValue(), prolabore.getPorcentagemImposto());
		Float valorParticipacaoTrabalho = participacaoService
				.obterValorParticipacaoTrabalho(
						prolabore.getPorcentagemParticipacaoTrabalho(),
						valorPreParticipacaoSemImposto, 0f, false, 0);

		List<PreParticipacaoFixo> listaPreParticipacaoFixos = new ArrayList<PreParticipacaoFixo>();
		listaPreParticipacaoFixos.addAll( prolabore.getListaPreParticipacaoFixos());

		List<Usuario> listaUsuarioPreParticipacao = new ArrayList<Usuario>();
		for (PreParticipacaoFixo preParticipacaoFixo : listaPreParticipacaoFixos) {
			listaUsuarioPreParticipacao.add(preParticipacaoFixo.getAdvogado());
		}

		// PreParticipacaoFixo pre;

		// //valor trbalho com redução de imposto
		// Float valorPreParticipacao = 0f;
		//
		// if
		// (prolabore.getValorParticipacaoEnum().equals(ValorParticipacaoEnum.VALOR)){
		// // valorPreParticipacao = pre.getValorParticipacao();
		// valorPreParticipacao = valorParticipacaoTrabalho;
		// }
		// else {
		// // valorPreParticipacao =
		// obterValorParticipacaoTrabalho(pre.getValorParticipacao(),valorParticipacaoTrabalho,0f,false,0);
		// valorPreParticipacao =
		// obterValorParticipacaoTrabalho(valorParticipacaoTrabalho,valorParticipacaoTrabalho,0f,false,0);
		//
		// }

		// Float valorTotalTrabalho =
		// (NumeroUtil.multiplicarDinheiro(trabalho.getValorTotalTrabalho(),(100
		// - percentagemImposto)/100));
		// lista de usuarios compartilhamento

		Boolean diferencasValores = false;

		List<Usuario> listaIndicados = prolabore.getCaso()
				.getListaUsuarioIndicacao();
		Participacao participacao;

		if (listaIndicados == null || listaIndicados.size() == 0) {
			System.out.println("Nao possui indicação... ");
//			throw new ControleException("Alguns casos não possuem indicação.");
		}

		List<Usuario> socios = sociosAplication.getListaSocios();

		for (Usuario usuario : socios) {
			participacao = new Participacao();
			float valorParticipacao = 0;

			participacao.setAdvogado(usuario);
			// inves da porcentagem, seta o valor totoal de sócio

			// valorParticipacao =
			// NumeroUtil.multiplicarDinheiro(NumeroUtil.multiplicarDinheiro(NumeroUtil.DividirDinheiro(fatura.getPorcentagemParticipacaoSocio(),
			// 100f), NumeroUtil.DividirDinheiro(usuario.getParticipacao() ,
			// 100f)), valorTotalTrabalho);

			valorParticipacao = obterValorParticipacaoSocio(fatura, usuario,
					valorPreParticipacaoSemImposto);
			participacao.setPorcentagemParticipacaoSocio(valorParticipacao);

			if (listaIndicados.contains(usuario)) {
				// divide a percentagem por 100. multiplica o valor disponivel
				// para indicacao pelo valor percentagem do advogado

				valorParticipacao = obterValorParticipacaoIndicacao(
						valorParticipacao, fatura, listaIndicados.size(),
						valorPreParticipacaoSemImposto);
				// valorParticipacao =
				// NumeroUtil.somarDinheiro(valorParticipacao,
				// NumeroUtil.multiplicarDinheiro(NumeroUtil.DividirDinheiro(fatura.getPorcentagemIndicacao(),
				// 100f),
				// (NumeroUtil.multiplicarDinheiro(NumeroUtil.DividirDinheiro(100f,
				// NumeroUtil.DividirDinheiro(Float.valueOf(listaIndicados.size()),
				// 100f)) , valorTotalTrabalho) )));
				//
				participacao.setPorcentagemIndicacao(obterPorcentagemIndicacao(
						fatura, listaIndicados.size(),
						valorPreParticipacaoSemImposto));

			}
			// adiciona participação caso advogado for o mesmo da
			// preParticipacao.
			if (listaUsuarioPreParticipacao.contains(usuario)) {
				float valorPre = 0f;

				for (PreParticipacaoFixo preParticipacaoFixo : listaPreParticipacaoFixos) {
					if (preParticipacaoFixo.getAdvogado().equals(usuario)) {
						// verifica se é valor ou porcentagem a distribuição de
						// participação criterio trablaho
						if (prolabore.getValorParticipacaoEnum().equals(
								ValorParticipacaoEnum.VALOR)) {
							valorPre = preParticipacaoFixo
									.getValorParticipacao();
						} else {
							valorPre = NumeroUtil.valorDaPorcentagem(
									valorParticipacaoTrabalho,
									preParticipacaoFixo.getValorParticipacao());
						}
					}
				}

				valorParticipacao = NumeroUtil.somarDinheiro(valorParticipacao,
						valorPre, 3);
				// valorParticipacao =
				// NumeroUtil.somarDinheiro(valorParticipacao,
				// NumeroUtil.multiplicarDinheiro(NumeroUtil.DividirDinheiro(fatura.getPorcentagemParticipacaoTrabalho(),
				// 100f), valorTotalTrabalho));

				participacao.setPorcentagemParticipacaoTrabalho(valorPre);

			}

			participacao.setFatura(fatura);

			participacao.setValorTotalParticipacao(valorParticipacao);

			participacao.setValorTotalPago(0f);
			participacao.setValorDisponivel(0f);

			// Inserir participacação no mapa e caso já estiver advogado com
			// participação realiaza atualização dos valores
			if (mapaParticipacao.get(participacao.getAdvogado().getIdUsuario()) != null) {
				float valorAtual = mapaParticipacao.get(
						participacao.getAdvogado().getIdUsuario())
						.getValorTotalParticipacao();
				float valorAdicional = participacao.getValorTotalParticipacao();
				float valorSocioAtual = mapaParticipacao.get(
						participacao.getAdvogado().getIdUsuario())
						.getPorcentagemParticipacaoSocio();
				float valorSocioAdicional = participacao
						.getPorcentagemParticipacaoSocio();
				float valorTrabalhoAtual = mapaParticipacao.get(
						participacao.getAdvogado().getIdUsuario())
						.getPorcentagemParticipacaoTrabalho();
				float valorTrabalhoAdicional = participacao
						.getPorcentagemParticipacaoTrabalho();
				float valorIndicacaoAtual = mapaParticipacao.get(
						participacao.getAdvogado().getIdUsuario())
						.getPorcentagemIndicacao();
				float valorIndicacaoAdicional = participacao
						.getPorcentagemIndicacao();

				mapaParticipacao.get(participacao.getAdvogado().getIdUsuario())
						.setValorTotalParticipacao(
								NumeroUtil.somarDinheiro(valorAtual,
										valorAdicional, 2));
				mapaParticipacao.get(participacao.getAdvogado().getIdUsuario())
						.setPorcentagemIndicacao(
								NumeroUtil.somarDinheiro(valorIndicacaoAtual,
										valorIndicacaoAdicional, 2));
				mapaParticipacao.get(participacao.getAdvogado().getIdUsuario())
						.setPorcentagemParticipacaoSocio(
								NumeroUtil.somarDinheiro(valorSocioAtual,
										valorSocioAdicional, 2));
				mapaParticipacao.get(participacao.getAdvogado().getIdUsuario())
						.setPorcentagemParticipacaoTrabalho(
								NumeroUtil.somarDinheiro(valorTrabalhoAtual,
										valorTrabalhoAdicional, 2));

			} else {
				mapaParticipacao.put(participacao.getAdvogado().getIdUsuario(),
						participacao);
			}

		}
		for (Usuario usuario : listaIndicados) {
			participacao = new Participacao();
			float valorParticipacao = 0;
			// verifica se já não é um dos socios, caso
			if (!socios.contains(usuario)) {
				participacao.setPorcentagemIndicacao(obterPorcentagemIndicacao(
						fatura, listaIndicados.size(),
						valorPreParticipacaoSemImposto));
				participacao.setAdvogado(usuario);
				valorParticipacao = obterValorParticipacaoIndicacao(
						valorParticipacao, fatura, listaIndicados.size(),
						valorPreParticipacaoSemImposto);

				if (listaUsuarioPreParticipacao.contains(usuario)) {
					float valorPre = 0f;

					for (PreParticipacaoFixo preParticipacaoFixo : listaPreParticipacaoFixos) {
						if (preParticipacaoFixo.getAdvogado().equals(usuario)) {
							if (prolabore.getValorParticipacaoEnum().equals(
									ValorParticipacaoEnum.VALOR)) {
								valorPre = preParticipacaoFixo
										.getValorParticipacao();
							} else {
								valorPre = NumeroUtil.valorDaPorcentagem(
										valorParticipacaoTrabalho,
										preParticipacaoFixo
												.getValorParticipacao());
							}
						}
					}

					participacao.setPorcentagemParticipacaoTrabalho(valorPre);
					valorParticipacao = NumeroUtil.somarDinheiro(
							valorParticipacao, valorPre, 3);
					participacao.setAdvogado(usuario);

				}

				// Só salva se tiver entrando nas condições
				if (participacao.getAdvogado() != null) {
					participacao.setValorTotalParticipacao(valorParticipacao);
					participacao.setFatura(fatura);
					participacao.setValorDisponivel(0f);
					participacao.setValorTotalPago(0f);

					// Inserir participacação no mapa e caso já estiver advogado
					// com participação realiaza atualização dos valores
					if (mapaParticipacao.get(participacao.getAdvogado()
							.getIdUsuario()) != null) {
						float valorAtual = mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.getValorTotalParticipacao();
						float valorAdicional = participacao
								.getValorTotalParticipacao();
						float valorSocioAtual = mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.getPorcentagemParticipacaoSocio();
						float valorSocioAdicional = participacao
								.getPorcentagemParticipacaoSocio();
						float valorTrabalhoAtual = mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.getPorcentagemParticipacaoTrabalho();
						float valorTrabalhoAdicional = participacao
								.getPorcentagemParticipacaoTrabalho();
						float valorIndicacaoAtual = mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.getPorcentagemIndicacao();
						float valorIndicacaoAdicional = participacao
								.getPorcentagemIndicacao();

						mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.setValorTotalParticipacao(
										valorAtual + valorAdicional);
						mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.setPorcentagemIndicacao(
										valorIndicacaoAtual
												+ valorIndicacaoAdicional);
						mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.setPorcentagemParticipacaoSocio(
										valorSocioAtual + valorSocioAdicional);
						mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.setPorcentagemParticipacaoTrabalho(
										valorTrabalhoAtual
												+ valorTrabalhoAdicional);
					} else {
						mapaParticipacao.put(participacao.getAdvogado()
								.getIdUsuario(), participacao);
					}

				}
			}

		}

		for (Usuario usuario2 : listaUsuarioPreParticipacao) {
			if (!socios.contains(usuario2)) {
				if (!listaIndicados.contains(usuario2)) {
					float valorPre = 0f;

					for (PreParticipacaoFixo preParticipacaoFixo : listaPreParticipacaoFixos) {
						if (preParticipacaoFixo.getAdvogado().equals(usuario2)) {
							if (prolabore.getValorParticipacaoEnum().equals(
									ValorParticipacaoEnum.VALOR)) {
								valorPre = preParticipacaoFixo
										.getValorParticipacao();
							} else {
								valorPre = NumeroUtil.valorDaPorcentagem(
										valorParticipacaoTrabalho,
										preParticipacaoFixo
												.getValorParticipacao());
							}
						}
					}

					participacao = new Participacao();

					participacao.setPorcentagemParticipacaoTrabalho(valorPre);
					participacao.setAdvogado(usuario2);
					participacao.setFatura(fatura);

					participacao.setValorTotalParticipacao(valorPre);
					participacao.setValorDisponivel(0f);
					participacao.setValorTotalPago(0f);

					// Inserir participacação no mapa e caso já estiver advogado
					// com participação realiaza atualização dos valores
					if (mapaParticipacao.get(participacao.getAdvogado()
							.getIdUsuario()) != null) {
						float valorAtual = mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.getValorTotalParticipacao();
						float valorAdicional = participacao
								.getValorTotalParticipacao();
						float valorSocioAtual = mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.getPorcentagemParticipacaoSocio();
						float valorSocioAdicional = participacao
								.getPorcentagemParticipacaoSocio();
						float valorTrabalhoAtual = mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.getPorcentagemParticipacaoTrabalho();
						float valorTrabalhoAdicional = participacao
								.getPorcentagemParticipacaoTrabalho();
						float valorIndicacaoAtual = mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.getPorcentagemIndicacao();
						float valorIndicacaoAdicional = participacao
								.getPorcentagemIndicacao();

						mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.setValorTotalParticipacao(
										valorAtual + valorAdicional);
						mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.setPorcentagemIndicacao(
										valorIndicacaoAtual
												+ valorIndicacaoAdicional);
						mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.setPorcentagemParticipacaoSocio(
										valorSocioAtual + valorSocioAdicional);
						mapaParticipacao.get(
								participacao.getAdvogado().getIdUsuario())
								.setPorcentagemParticipacaoTrabalho(
										valorTrabalhoAtual
												+ valorTrabalhoAdicional);
					} else {
						mapaParticipacao.put(participacao.getAdvogado()
								.getIdUsuario(), participacao);
					}

				}
			}

		}

		// cadastrar imposto

		participacao = new Participacao();
		participacao.setImposto(true);
		participacao.setFatura(fatura);
		participacao.setValorTotalParticipacao(obterValorImposto(fatura,
				prolabore.getValor().floatValue()));

		participacao.setValorDisponivel(0f);
		participacao.setValorTotalPago(0f);

		if (mapaParticipacao.get(-1) != null) {
			float valorAtual = mapaParticipacao.get(-1)
					.getValorTotalParticipacao();
			float valorAdicional = participacao.getValorTotalParticipacao();
			mapaParticipacao.get(-1).setValorTotalParticipacao(
					valorAtual + valorAdicional);
		} else {
			mapaParticipacao.put(-1, participacao);
		}

		// cadastrar fundo

		participacao = new Participacao();
		participacao.setFundo(true);
		participacao.setFatura(fatura);
		participacao.setValorTotalParticipacao(obterValotFundo(fatura,
				valorPreParticipacaoSemImposto));

		participacao.setValorDisponivel(0f);
		participacao.setValorTotalPago(0f);

		// Inserir participacação no mapa e caso já estiver advogado com
		// participação realiaza atualização dos valores
		if (mapaParticipacao.get(0) != null) {
			float valorAtual = mapaParticipacao.get(0)
					.getValorTotalParticipacao();
			float valorAdicional = participacao.getValorTotalParticipacao();
			mapaParticipacao.get(0).setValorTotalParticipacao(
					valorAtual + valorAdicional);
		} else {
			mapaParticipacao.put(0, participacao);
		}

		// percorre o mapa e realiza a persistência dos dados
		Collection<Participacao> cont = mapaParticipacao.values();
		Iterator<Participacao> i = cont.iterator();
		while (i.hasNext()) {
			if (i != null) {
				Participacao item = i.next();
				valorSomaParticipacao = NumeroUtil.somarDinheiro(
						valorSomaParticipacao,
						item.getValorTotalParticipacao(), 3);

				item.setValorDisponivel(NumeroUtil.deixarFloatDuasCasas(item
						.getValorDisponivel()));
				item.setValorTotalPago(NumeroUtil.deixarFloatDuasCasas(item
						.getValorTotalPago()));
				item.setValorTotalParticipacao(NumeroUtil
						.deixarFloatDuasCasas(item.getValorTotalParticipacao()));

				em.persist(item);
			}
		}

		// verifica se a soma das participações é igual ao valor fatura

		if (!prolabore.getPreParticipacaoCadastrada()) {
			valorSomaParticipacao = NumeroUtil.somarDinheiro(
					valorSomaParticipacao, valorParticipacaoTrabalho, 3);
		}

		if (!valorSomaParticipacao.equals(fatura.valorTotalFatura())) {
			// Obtem a diferença e insere no fundo
			Float dieferenca = NumeroUtil.diminuirDinheiro(
					fatura.valorTotalFatura(), valorSomaParticipacao, 3);
			dieferenca = NumeroUtil.deixarFloatDuasCasas(dieferenca);
			
			if (dieferenca < 2f) {
				Participacao fundo = mapaParticipacao.get(0);
				
				Float valorNovo = NumeroUtil.somarDinheiro(
						fundo.getValorTotalParticipacao(), dieferenca, 3);
				valorNovo = NumeroUtil.deixarFloatDuasCasas(valorNovo);
				
				fundo.setValorTotalParticipacao(valorNovo);
				
				em.persist(fundo);
			} else {
				System.out.println("Diferença é maior que 2: " + dieferenca);
			}

		}

	}

	private Float obterPorcentagemParticipacaoTrabalho(Fatura fatura,
			Float valorTotalTrabalho, boolean temCompartilhamento,
			Integer qunatidadeCompartilhamento) {

		// ((fatura.getPorcentagemParticipacaoTrabalho() / 100) *
		// valorTotalTrabalho));
		Float retorno = 0f;
		Float valor1 = NumeroUtil.DividirDinheiro(
				fatura.getPorcentagemParticipacaoTrabalho(), 100f, 3);
		Float valor3 = NumeroUtil.multiplicarDinheiro(valor1,
				valorTotalTrabalho, 3);

		if (temCompartilhamento) {
			retorno = NumeroUtil.DividirDinheiro(valor3,
					qunatidadeCompartilhamento.floatValue(), 3);
		} else {
			retorno = valor3;
		}

		retorno = NumeroUtil.deixarFloatDuasCasas(retorno);

		return retorno;
	}

	private Float obterPorcentagemIndicacao(Fatura fatura, Integer size,
			Float valorTotalTrabalho) {

		Float retorno = 0f;

		// ( ( fatura.getPorcentagemIndicacao()/100 )*( (100f /
		// listaIndicados.size()) /100 ) * valorTotalTrabalho )

		Float valor1 = NumeroUtil.DividirDinheiro(
				fatura.getPorcentagemIndicacao(), 100f, 3);
		Float valor2 = NumeroUtil.DividirDinheiro(100f, size.floatValue(), 3);
		Float valor3 = NumeroUtil.DividirDinheiro(valor2, 100f, 3);
		Float valor4 = NumeroUtil.multiplicarDinheiro(valor1, valor3, 3);
		retorno = NumeroUtil.multiplicarDinheiro(valor4, valorTotalTrabalho, 3);

		retorno = NumeroUtil.deixarFloatDuasCasas(retorno);

		return retorno;
	}

	public Float obterValorTotalTrabalhoSemImposto(Float valorToalTrabalho,
			Float percentagemImposto) {

		// (trabalho.getValorTotalTrabalho() * (100 - percentagemImposto) /100)
		Float retorno = 0f;
		// (100 - percentagemImposto)
		Float valor1 = NumeroUtil.diminuirDinheiro(100f, percentagemImposto, 3);
		// (100 - percentagemImposto) /100)
		Float valor2 = NumeroUtil.DividirDinheiro(valor1, 100f, 3);

		retorno = NumeroUtil.multiplicarDinheiro(valor2, valorToalTrabalho, 3);

		return NumeroUtil.deixarFloatDuasCasas(retorno);
	}

	private Float obterValorImposto(Fatura fatura, Float valorTotalTrabalho) {

		// (fatura.getPorcentagemImposto() / 100) *
		// trabalho.getValorTotalTrabalho()
		Float retorno = 0f;
		// (fatura.getPorcentagemImposto() / 100)
		Float valor1 = NumeroUtil.DividirDinheiro(
				fatura.getPorcentagemImposto(), 100f, 3);

		retorno = NumeroUtil.multiplicarDinheiro(valor1, valorTotalTrabalho, 3);

		return NumeroUtil.deixarFloatDuasCasas(retorno);
	}

	private Float obterValotFundo(Fatura fatura, Float valorTotalTrabalho) {
		Float retorno = 0f;

		// (fatura.getPorcentagemFundo() / 100) * valorTotalTrabalho)

		Float valor1 = NumeroUtil.DividirDinheiro(fatura.getPorcentagemFundo(),
				100f, 3);
		retorno = NumeroUtil.multiplicarDinheiro(valor1, valorTotalTrabalho, 2);

		return NumeroUtil.deixarFloatDuasCasas(retorno);
	}

	private float obterValorParticipacaoIndicacao(Float valorParticipacao,
			Fatura fatura, Integer size, Float valorTotalTrabalho) {
		// TODO Auto-generated method stub
		//
		// valorParticipacao + ( (fatura.getPorcentagemIndicacao()/(100))*(
		// (100f / listaIndicados.size()) /100) * valorTotalTrabalho );

		Float retorno = 0f;
		// fatura.getPorcentagemIndicacao()/(100)
		Float valor1 = NumeroUtil.DividirDinheiro(
				fatura.getPorcentagemIndicacao(), 100f, 3);
		// listaIndicados.size())/100
		Float valor2 = NumeroUtil.DividirDinheiro(100f, size.floatValue(), 3);

		// (100f / listaIndicados.size()) /100)
		Float valor3 = NumeroUtil.DividirDinheiro(valor2, 100f, 3);

		Float valor4 = NumeroUtil.multiplicarDinheiro(valor1, valor3, 3);

		Float valor5 = NumeroUtil.multiplicarDinheiro(valor4,
				valorTotalTrabalho, 3);

		retorno = NumeroUtil.somarDinheiro(valorParticipacao, valor5, 2);

		return NumeroUtil.deixarFloatDuasCasas(retorno);
	}

	public float obterValorParticipacaoTrabalho(Float porcentagem,
			Float valorTotalTrabalho, Float valorParticipacao,
			boolean temCompartilhamento, Integer qunatidadeCompartilhamento) {
		Float retorno = 0f;

		// ((fatura.getPorcentagemParticipacaoTrabalho() / 100) *
		// valorTotalTrabalho);

		Float valor1 = NumeroUtil.DividirDinheiro(porcentagem, 100f, 3);
		Float valor2 = NumeroUtil.multiplicarDinheiro(valor1,
				valorTotalTrabalho, 3);

		Float valor3 = NumeroUtil.somarDinheiro(valorParticipacao, valor2, 2);

		if (temCompartilhamento) {
			retorno = NumeroUtil.DividirDinheiro(valor3,
					qunatidadeCompartilhamento.floatValue(), 3);
		} else {
			retorno = valor3;
		}

		return NumeroUtil.deixarFloatDuasCasas(retorno);
	}

	// usado para calcular o valor participação de sócio
	private Float obterValorParticipacaoSocio(Fatura fatura, Usuario usuario,
			Float valorTotalTrabalho) {

		Float resultado = 0f;

		// expressão em float (fatura.getPorcentagemParticipacaoSocio() /
		// 100)*(usuario.getParticipacao() / 100) * valorTotalTrabalho ;

		// (fatura.getPorcentagemParticipacaoSocio() / 100)
		Float valor1 = NumeroUtil.DividirDinheiro(
				fatura.getPorcentagemParticipacaoSocio(), 100f, 3);

		// (usuario.getParticipacao() / 100)
		Float valor2 = NumeroUtil.DividirDinheiro(usuario.getParticipacao(),
				100f, 3);

		// (fatura.getPorcentagemParticipacaoSocio() /
		// 100)*(usuario.getParticipacao() / 100)
		Float valor3 = NumeroUtil.multiplicarDinheiro(valor1, valor2, 3);

		// (fatura.getPorcentagemParticipacaoSocio() /
		// 100)*(usuario.getParticipacao() / 100) * valorTotalTrabalho ;
		resultado = NumeroUtil.multiplicarDinheiro(valor3, valorTotalTrabalho,
				2);

		return NumeroUtil.deixarFloatDuasCasas(resultado);
	}

	public void alterarParticipacao(Participacao participacao) {
		em.merge(participacao);
	}

	// TODO rever
	public ParticipacaoFaturaDTO listarParticiapcaoFaturaPorFatura(Fatura fatura) {
		ParticipacaoFaturaDTO participacaoFaturaDTO = new ParticipacaoFaturaDTO();
		participacaoFaturaDTO.setFatura(fatura);

		Map<Integer, ItemParticipacaoFaturaDTO> mapaItem = new TreeMap<Integer, ItemParticipacaoFaturaDTO>();

		for (Trabalho trabalho : fatura.getListaTrabalhos()) {

			for (Participacao participacao : participacaoService
					.listarParticipacaoPorFatura(fatura.getIdFatura())) {

				// verifica primeiro se é imposto e fundo
				if (participacao.getImposto() != null
						&& participacao.getImposto()) {
					if (mapaItem.containsKey(-1)) {
						ItemParticipacaoFaturaDTO item = mapaItem.get(-1);
						item.setValor(item.getValor()
								+ participacao.getValorTotalParticipacao());
					} else {
						ItemParticipacaoFaturaDTO item = new ItemParticipacaoFaturaDTO();
						item.setNome("Imposto");
						item.setValor(participacao.getValorTotalParticipacao());
						item.setIdItem(-1);
						mapaItem.put(-1, item);
					}

				} else if (participacao.getFundo() != null
						&& participacao.getFundo()) {
					if (mapaItem.containsKey(0)) {
						ItemParticipacaoFaturaDTO item = mapaItem.get(0);
						item.setValor(item.getValor()
								+ participacao.getValorTotalParticipacao());
					} else {
						ItemParticipacaoFaturaDTO item = new ItemParticipacaoFaturaDTO();
						item.setNome("Fundo");
						item.setValor(participacao.getValorTotalParticipacao());
						item.setIdItem(0);
						mapaItem.put(0, item);
					}

				} else if (mapaItem.containsKey(participacao.getAdvogado()
						.getIdUsuario())) {
					ItemParticipacaoFaturaDTO item = mapaItem.get(participacao
							.getAdvogado().getIdUsuario());
					item.setValor(item.getValor()
							+ participacao.getValorTotalParticipacao());

				} else {
					ItemParticipacaoFaturaDTO item = new ItemParticipacaoFaturaDTO();
					item.setNome(participacao.getAdvogado().getNome());
					item.setValor(participacao.getValorTotalParticipacao());
					item.setIdItem(participacao.getAdvogado().getIdUsuario());
					mapaItem.put(participacao.getAdvogado().getIdUsuario(),
							item);
				}

			}
		}

		// Inserir conteudo do mapa dentro da lista da DTO
		List<ItemParticipacaoFaturaDTO> listaItem = new ArrayList<ItemParticipacaoFaturaDTO>();

		Set<Integer> chaves = mapaItem.keySet();
		for (Integer chave : chaves) {
			listaItem.add(mapaItem.get(chave));

		}

		participacaoFaturaDTO.setListItem(listaItem);

		return participacaoFaturaDTO;
	}

	public List<Participacao> listarParticipacaoPorFatura(Integer idFatura) {
		String sql = "FROM Participacao p WHERE p.fatura.idFatura = :idFatura";
		Query query = em.createQuery(sql);
		query.setParameter("idFatura", idFatura);

		return query.getResultList();
	}

	/**
	 * Retorna uma lista de participações por transacção. Usado quando se delete
	 * uma transacação, obtém todas as participações da transacação..
	 * 
	 * @param idFatura
	 * @return
	 */
	public List<Participacao> listarParticipacaoPorTransacao(Integer idTransacao) {
		String sql = "FROM Participacao p WHERE p.transacaoPagamentoAdvogado.idTranasacao = :idTranasacao";
		Query query = em.createQuery(sql);
		query.setParameter("idTranasacao", idTransacao);

		return query.getResultList();
	}

	public void exlcuirParticipacaoPorFatura(Integer idFatura, boolean trabalho) throws Exception {
		String sql = "DELETE Participacao p where p.fatura.idFatura = :idFatura";
		
		if (trabalho) {
			sql = sql + " AND p.porcentagemParticipacaoTrabalho > 0";
		}
		
		Query query = em.createQuery(sql);
		query.setParameter("idFatura", idFatura);

		query.executeUpdate();

	}

	// detalhar participação por advogado.
	public List<Participacao> listarParticipacaoPorAdvogado(Integer idAdvogado,
			Integer idCliente, Date dataInicial, Date dataFinal) {
		StringBuilder sql = new StringBuilder(
				"FROM Participacao p WHERE p.advogado.idUsuario = :idAdvogado ");

		if (idCliente != null) {
			sql.append(" AND p.trabalho.caso.cliente.idCliente = :idCliente ");
		}

		TypedQuery<Participacao> query = em.createQuery(sql.toString(),
				Participacao.class);
		query.setParameter("idAdvogado", idAdvogado);

		if (idCliente != null) {
			query.setParameter("idCliente", idCliente);
		}

		List<Participacao> listaParticipacao = query.getResultList();

		return listaParticipacao;

	}

	public List<Participacao> listarParticipacaoPendentePorAdvogado(
			Integer idAdvogado) {
		String sql = "FROM Participacao p where p.advogado.idUsuario = :idAdvogado AND (p.fatura.statusFatura = :faturado OR p.fatura.statusFatura = :parcial) ";
		Query query = em.createQuery(sql, Participacao.class);

		query.setParameter("idAdvogado", idAdvogado);
		query.setParameter("faturado", StatusTrabalho.FATURADO);
		query.setParameter("parcial", StatusTrabalho.PARCIAL_PAGO);

		return query.getResultList();
	}

	// Busca uma lista de participações onde o cliente já pagou alguma coisa da
	// fatura relacionada em ordem de data.

	public List<Participacao> listarParticipacaoDisponivelPorAdvogado(
			Integer idAdvogado) {
		String sql = "FROM Participacao p where p.advogado.idUsuario = :idAdvogado AND (p.fatura.statusFatura = :faturado OR p.fatura.statusFatura = :parcial) order by p.dataPagamentoParticipacao";
		Query query = em.createQuery(sql, Participacao.class);

		query.setParameter("idAdvogado", idAdvogado);
		query.setParameter("faturado", StatusTrabalho.PAGO);
		query.setParameter("parcial", StatusTrabalho.PARCIAL_PAGO);

		return query.getResultList();
	}

	public List<Participacao> pesquisarParticipacaoAdvogado(
			ItemParticipacaoUsuarioDTO item) {
		String sql = "FROM Participacao p where p.advogado.idUsuario = :idAdvogado";
		Query query = em.createQuery(sql, Participacao.class);
		query.setParameter("idAdvogado", item.getIdItem());

		return query.getResultList();
	}

	// deetalhes da listagem de participação por adovogado. Esse método irá
	// listar todas as faturas e valores que o advogado em questão tem parti
	// cipação
	// TODO rever método
	public List<DetalhesParticipacaoAdvogadoDTO> pesquisarParticipacao(
			Integer idCliente, Integer idAdvogado, Date dataInicioPesquisa,
			Date dataFimPesquisa) {
		StringBuilder hql = new StringBuilder();
		hql.append("FROM Participacao p WHERE ");

		if (dataInicioPesquisa != null) {
			hql.append(" p.fatura.dataFatura >= :dataInicio AND ");
		}

		if (dataFimPesquisa != null) {
			hql.append(" p.fatura.dataFatura <= :dataFim AND ");
		}

		if (idCliente != null && idCliente > 0) {
			hql.append(" p.fatura.cliente.idCliente = :idCliente AND ");
		}
		if (idAdvogado != null) {
			hql.append(" p.advogado.idUsuario = :idUsuario AND ");
		}

		// isso é feito pois não é realizado pagamentos menores que o total
		hql.append(" (p.valorTotalPago = 0 OR p.valorDisponivel <> 0)  order by p.valorDisponivel, p.fatura.cliente.nome, p.fatura.dataFatura desc");

		TypedQuery<Participacao> query = em.createQuery(hql.toString(),
				Participacao.class);

		if (dataInicioPesquisa != null) {
			query.setParameter("dataInicio", dataInicioPesquisa);
		}

		if (dataFimPesquisa != null) {
			query.setParameter("dataFim", dataFimPesquisa);
		}

		if (idCliente != null && idCliente > 0) {
			query.setParameter("idCliente", idCliente);
		}
		if (idAdvogado != null) {
			hql.append(" p.advogado.idUsuario = :idUsuario AND ");
			query.setParameter("idUsuario", idAdvogado);
		}

		List<Participacao> participacoes = query.getResultList();
		List<DetalhesParticipacaoAdvogadoDTO> detalhes = new ArrayList<DetalhesParticipacaoAdvogadoDTO>();

		Map<Integer, DetalhesParticipacaoAdvogadoDTO> dtoMap = new HashMap<Integer, DetalhesParticipacaoAdvogadoDTO>();

		for (Participacao participacao : participacoes) {
			if (dtoMap.containsKey(participacao.getFatura().getCliente()
					.getIdCliente())) {
				DetalhesParticipacaoAdvogadoDTO dto = dtoMap.get(participacao
						.getFatura().getCliente().getIdCliente());
				dto.setValorDisponivel(dto.getValorDisponivel()
						+ participacao.getValorDisponivel());
				dto.setValorPendente(dto.getValorPendente()
						+ (participacao.getValorTotalParticipacao()
								- participacao.getValorTotalPago() - participacao
									.getValorDisponivel()));
				dto.setValorPago(dto.getValorPago()
						+ participacao.getValorTotalPago());

				dto.getListaParticipacao().add(participacao);

			} else {
				DetalhesParticipacaoAdvogadoDTO dto = new DetalhesParticipacaoAdvogadoDTO();
				dto.setIdAdvogado(participacao.getAdvogado().getIdUsuario());
				dto.setIdCliente(participacao.getFatura().getCliente()
						.getIdCliente());
				dto.setNomeAdvogado(participacao.getAdvogado().getNome());
				dto.setNomeCliente(participacao.getFatura().getCliente()
						.getNome());
				dto.setValorDisponivel(participacao.getValorDisponivel());
				dto.setValorPago(participacao.getValorTotalPago());
				dto.setValorPendente(participacao.getValorTotalParticipacao()
						- participacao.getValorTotalPago()
						- participacao.getValorDisponivel());

				dto.getListaParticipacao().add(participacao);

				dtoMap.put(dto.getIdCliente(), dto);

			}

		}

		Collection<DetalhesParticipacaoAdvogadoDTO> cont = dtoMap.values();
		Iterator<DetalhesParticipacaoAdvogadoDTO> i = cont.iterator();
		while (i.hasNext()) {
			if (i != null) {
				DetalhesParticipacaoAdvogadoDTO item = i.next();
				detalhes.add(item);
			}
		}

		System.out.println("teste");

		// Trecho para ordenar a lista pelo nome cliente
		Collections.sort(detalhes, new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				DetalhesParticipacaoAdvogadoDTO cf1 = (DetalhesParticipacaoAdvogadoDTO) o1;
				DetalhesParticipacaoAdvogadoDTO cf2 = (DetalhesParticipacaoAdvogadoDTO) o2;

				return new Float(cf2.getValorDisponivel()).compareTo(new Float(
						cf1.getValorDisponivel()));

			}
		});

		return detalhes;

	}

	// Realiza o pagamento para o advogado de uma lista de participações.
	// Cria transações, desconta os valor da conta
	public void pagarParticipacaoOldDeletar(
			List<ParticipacaoUsuarioDTO> listaParticipacaoDTO) {
		for (ParticipacaoUsuarioDTO participacaoUsuarioDTO : listaParticipacaoDTO) {
			if (participacaoUsuarioDTO.getDtoSelecionado()
					&& participacaoUsuarioDTO.getValorParaReceber() != null) {
				Conta conta = contaService
						.obterContaPorAdvogado(participacaoUsuarioDTO
								.getIdUsuario());
				Transacao transacao = new Transacao();
				transacao.setConta(conta);
				transacao.setDataTransacao(participacaoUsuarioDTO
						.getDataRecebimento());
				transacao.setTipoTransacaoEnum(TipoTransacaoEnum.PAGAMENTO);
				transacao.setValorTransacao(participacaoUsuarioDTO
						.getValorParaReceber());

				// inserindo transacao
				transacaoService.inserirTransacao(transacao);

				// obtendo todas as participações
				List<Participacao> listaParticipacaoDisponivel = participacaoService
						.listarParticipacaoDisponivelPorAdvogado(participacaoUsuarioDTO
								.getIdUsuario());

				// ele recebe o valor atual de pagamento e no for vai realizando
				// a baixa até ficar zerado.
				Float valorParaDesconto = participacaoUsuarioDTO
						.getValorParaReceber();
				Float valorDisponivelDuasCasas = NumeroUtil
						.deixarFloatDuasCasas(participacaoUsuarioDTO
								.getValorDisponivel());

				// Alterando as participações
				if (valorDisponivelDuasCasas.equals(participacaoUsuarioDTO
						.getValorParaReceber())) {
					for (Participacao participacao : listaParticipacaoDisponivel) {
						participacao.setValorDisponivel(0f);
						participacao.setValorTotalPago(participacao
								.getValorTotalParticipacao());
						participacaoService.alterarParticipacao(participacao);
					}
				} else {
					for (Participacao participacao : listaParticipacaoDisponivel) {
						// Caso o valor disponivel for menor ou igual ao valor
						// de desconto (valor de desonto é o valor total pago
						// para o advogado)
						//
						if (participacao.getValorDisponivel() <= valorParaDesconto) {
							valorParaDesconto = valorParaDesconto
									- participacao.getValorDisponivel();
							participacao.setValorTotalPago(participacao
									.getValorTotalPago()
									+ participacao.getValorDisponivel());
							participacao.setValorDisponivel(0f);
							participacaoService
									.alterarParticipacao(participacao);

						}
						// se o valor disponível for maior,
						else {
							participacao.setValorTotalPago(participacao
									.getValorTotalPago() + valorParaDesconto);
							participacao.setValorDisponivel(participacao
									.getValorDisponivel() - valorParaDesconto);
							participacaoService
									.alterarParticipacao(participacao);
							valorParaDesconto = 0f;
							break;
						}

					}
				}
				//

				// alterando conta
				conta.setValorDisponivel(conta.getValorDisponivel()
						- participacaoUsuarioDTO.getValorParaReceber());
				contaService.alterarConta(conta);

			}
		}

	}

	public void pagarParticipacao(
			List<DetalhesParticipacaoAdvogadoDTO> listaDeralhesParticipacaoDTO,
			Integer idCliente, Integer idAdvogado) {

		Cliente cliente = clienteService.obterCliente(idCliente);
		Usuario advogado = usuarioService.obterUsuarioPorId(idAdvogado);
		Conta conta = contaService.obterContaPorAdvogado(advogado
				.getIdUsuario());

		List<Participacao> listaParticipacao = new ArrayList<Participacao>();

		Float valorTotal = 0F;

		// alterando os valores das participações
		for (DetalhesParticipacaoAdvogadoDTO dto : listaDeralhesParticipacaoDTO) {
			// Ultima validação para que não realize pagamentos mais de uma vez
			// do valorPendente
			if (dto.getDtoSelecionada()
					&& ((dto.getValorDisponivel() != null && dto
							.getValorDisponivel() > 0) || (dto
							.getValorPendente() != null && dto
							.getValorPendente() > 0))
					&& dto.getValorDisponivel() >= 0) {
				boolean emprestimo = true;

				if (dto.getValorDisponivel() != null
						&& dto.getValorDisponivel() > 0) {
					// valorTotal + dto.getValorDisponivel();
					valorTotal = NumeroUtil.somarDinheiro(valorTotal,
							dto.getValorDisponivel(), 3);
					emprestimo = false;
				} else if (dto.getValorPendente() != null
						&& dto.getValorPendente() > 0) {
					// valorTotal = valorTotal + dto.getValorPendente();
					valorTotal = NumeroUtil.somarDinheiro(valorTotal,
							dto.getValorPendente(), 3);
					emprestimo = true;
				}

				for (Participacao participacao : dto.getListaParticipacao()) {
					if (!emprestimo) {
						participacao.setValorTotalPago(participacao
								.getValorDisponivel());
						participacao.setValorDisponivel(0f);
					} else {
						participacao.setValorTotalPago(participacao
								.getValorTotalParticipacao());

						Float valorDisponivel = NumeroUtil.multiplicarDinheiro(
								participacao.getValorTotalParticipacao(), -1f,
								3);

						participacao.setValorDisponivel(NumeroUtil
								.deixarFloatDuasCasas(valorDisponivel));
					}

					participacaoService.alterarParticipacao(participacao);

					listaParticipacao.add(participacao);
				}

			}

		}

		valorTotal = NumeroUtil.deixarFloatDuasCasas(valorTotal);

		if (valorTotal > 0) {
			// criando transacao
			Transacao transacao = new Transacao();

			transacao.setConta(conta);
			transacao.setDataTransacao(new Date());
			transacao.setTipoTransacaoEnum(TipoTransacaoEnum.PAGAMENTO);
			transacao.setValorTransacao(valorTotal);

			transacaoService.inserirTransacao(transacao);

			// atrelando a transacao para participações
			for (Participacao participacao : listaParticipacao) {
				participacao.setTransacaoPagamentoAdvogado(transacao);
				participacaoService.alterarParticipacao(participacao);
			}

			// Alterando a conta do usuário
			System.out.println("ADV: " + conta.getAdvogado().getNome());
			System.out.println("VALOR TOTAL: " + valorTotal);
			System.out.println("DISPONIVEL : " + conta.getValorDisponivel());
			System.out
					.println("===============================================");

			Float valorDisponivel = NumeroUtil.diminuirDinheiro(
					conta.getValorDisponivel(), valorTotal, 3);

			conta.setValorDisponivel(NumeroUtil
					.deixarFloatDuasCasas(valorDisponivel));
			contaService.alterarConta(conta);

		}
	}

	public SociosAplication getSociosAplication() {
		return sociosAplication;
	}

	public void setSociosAplication(SociosAplication sociosAplication) {
		this.sociosAplication = sociosAplication;
	}

	public List<Usuario> getListaUsuarioCombo() {
		return listaUsuarioCombo;
	}

	public void setListaUsuarioCombo(List<Usuario> listaUsuarioCombo) {
		this.listaUsuarioCombo = listaUsuarioCombo;
	}

	public List<Cliente> getListaClienteCombo() {
		return listaClienteCombo;
	}

	public void setListaClienteCombo(List<Cliente> listaClienteCombo) {
		this.listaClienteCombo = listaClienteCombo;
	}

	public List<Caso> getListaCasoCombo() {
		return listaCasoCombo;
	}

	public void setListaCasoCombo(List<Caso> listaCasoCombo) {
		this.listaCasoCombo = listaCasoCombo;
	}

	public void cadastrarParticipacaoExito(Caso caso, Fatura fatura) {
		List<Participacao> listaParticipacao = new ArrayList<Participacao>();
		// usado para dividir pelo valor total de participaao

		// Atualizar fatura para que as porcetagens sejam salvas
		faturaService.atualizarFatura(fatura);

		Map<Integer, Participacao> mapaParticipacao = new HashMap<Integer, Participacao>();

		Float percentagemImposto = fatura.getPorcentagemImposto();

		Float valorSomaParticipacao = 0f;

		// valor trbalho com redução de imposto
		Float valorTotalExitoSemImposto = obterValorTotalTrabalhoSemImposto(
				fatura.getParcelaExito().getValor(), percentagemImposto);
		Float valorDistribuicaoTrabalho = obterValorParticipacaoTrabalho(
				fatura.getPorcentagemParticipacaoTrabalho(),
				valorTotalExitoSemImposto, 0f, false, 0);

		for (PreParticipacaoExito pre : caso.getListaPreParticipacaoExito()) {
			Participacao p = new Participacao();
			p.setAdvogado(pre.getAdvogado());
			p.setValorTotalParticipacao(NumeroUtil.porcentagem(
					valorDistribuicaoTrabalho, pre.getValorParticipacao()));
			p.setPorcentagemParticipacaoTrabalho(NumeroUtil.porcentagem(
					valorDistribuicaoTrabalho, pre.getValorParticipacao()));
			listaParticipacao.add(p);
		}

		List<Usuario> listaIndicados = caso.getListaUsuarioIndicacao();

		if (listaIndicados == null || listaIndicados.size() == 0) {
			throw new ControleException("Alguns casos não possuem indicação.");
		}

		for (Usuario usuario : sociosAplication.getListaSocios()) {
			Participacao participacao = new Participacao();
			participacao
					.setPorcentagemParticipacaoSocio(obterValorParticipacaoSocio(
							fatura, usuario, valorTotalExitoSemImposto));
			participacao.setValorTotalParticipacao(obterValorParticipacaoSocio(
					fatura, usuario, valorTotalExitoSemImposto));
			participacao.setAdvogado(usuario);

			listaParticipacao.add(participacao);
		}

		for (Usuario usuario : listaIndicados) {
			Participacao participacao = new Participacao();
			participacao
					.setPorcentagemIndicacao(obterValorParticipacaoIndicacao(
							0f, fatura, listaIndicados.size(),
							valorTotalExitoSemImposto));
			participacao
					.setValorTotalParticipacao(obterValorParticipacaoIndicacao(
							0f, fatura, listaIndicados.size(),
							valorTotalExitoSemImposto));
			participacao.setAdvogado(usuario);
			listaParticipacao.add(participacao);
		}

		for (Participacao participacao : listaParticipacao) {
			if (mapaParticipacao.get(participacao.getAdvogado().getIdUsuario()) != null) {
				float valorAtual = mapaParticipacao.get(
						participacao.getAdvogado().getIdUsuario())
						.getValorTotalParticipacao();
				float valorAdicional = participacao.getValorTotalParticipacao();
				float valorSocioAtual = mapaParticipacao.get(
						participacao.getAdvogado().getIdUsuario())
						.getPorcentagemParticipacaoSocio();
				float valorSocioAdicional = participacao
						.getPorcentagemParticipacaoSocio();
				float valorTrabalhoAtual = mapaParticipacao.get(
						participacao.getAdvogado().getIdUsuario())
						.getPorcentagemParticipacaoTrabalho();
				float valorTrabalhoAdicional = participacao
						.getPorcentagemParticipacaoTrabalho();
				float valorIndicacaoAtual = mapaParticipacao.get(
						participacao.getAdvogado().getIdUsuario())
						.getPorcentagemIndicacao();
				float valorIndicacaoAdicional = participacao
						.getPorcentagemIndicacao();

				mapaParticipacao.get(participacao.getAdvogado().getIdUsuario())
						.setValorTotalParticipacao(
								NumeroUtil.somarDinheiro(valorAtual,
										valorAdicional, 3));
				mapaParticipacao.get(participacao.getAdvogado().getIdUsuario())
						.setPorcentagemIndicacao(
								NumeroUtil.somarDinheiro(valorIndicacaoAtual,
										valorIndicacaoAdicional, 3));
				mapaParticipacao.get(participacao.getAdvogado().getIdUsuario())
						.setPorcentagemParticipacaoSocio(
								NumeroUtil.somarDinheiro(valorSocioAtual,
										valorSocioAdicional, 3));
				mapaParticipacao.get(participacao.getAdvogado().getIdUsuario())
						.setPorcentagemParticipacaoTrabalho(
								NumeroUtil.somarDinheiro(valorTrabalhoAtual,
										valorTrabalhoAdicional, 3));

			} else {
				mapaParticipacao.put(participacao.getAdvogado().getIdUsuario(),
						participacao);
			}
		}

		// cadastrar imposto

		Participacao participacao = new Participacao();
		participacao.setImposto(true);
		participacao.setFatura(fatura);
		participacao.setValorTotalParticipacao(obterValorImposto(fatura,
				fatura.valorTotalFatura()));

		participacao.setValorDisponivel(0f);
		participacao.setValorTotalPago(0f);

		if (mapaParticipacao.get(-1) != null) {
			float valorAtual = mapaParticipacao.get(-1)
					.getValorTotalParticipacao();
			float valorAdicional = participacao.getValorTotalParticipacao();
			mapaParticipacao.get(-1).setValorTotalParticipacao(
					valorAtual + valorAdicional);
		} else {
			mapaParticipacao.put(-1, participacao);
		}

		// cadastrar fundo

		participacao = new Participacao();
		participacao.setFundo(true);
		participacao.setFatura(fatura);
		participacao.setValorTotalParticipacao(obterValotFundo(fatura,
				valorTotalExitoSemImposto));

		participacao.setValorDisponivel(0f);
		participacao.setValorTotalPago(0f);

		// Inserir participacação no mapa e caso já estiver advogado com
		// participação realiaza atualização dos valores
		if (mapaParticipacao.get(0) != null) {
			float valorAtual = mapaParticipacao.get(0)
					.getValorTotalParticipacao();
			float valorAdicional = participacao.getValorTotalParticipacao();
			mapaParticipacao.get(0).setValorTotalParticipacao(
					valorAtual + valorAdicional);
		} else {
			mapaParticipacao.put(0, participacao);
		}

		// percorre o mapa e realiza a persistência dos dados
		Collection<Participacao> cont = mapaParticipacao.values();
		Iterator<Participacao> i = cont.iterator();
		while (i.hasNext()) {
			if (i != null) {
				Participacao item = i.next();
				valorSomaParticipacao = NumeroUtil.somarDinheiro(
						valorSomaParticipacao,
						item.getValorTotalParticipacao(), 3);

				item.setValorDisponivel(0f);
				item.setValorTotalPago(0f);
				item.setValorTotalParticipacao(NumeroUtil
						.deixarFloatDuasCasas(item.getValorTotalParticipacao()));
				item.setFatura(fatura);
				em.persist(item);
			}
		}

		// verifica se a soma das participações é igual ao valor fatura
		if (!valorSomaParticipacao.equals(fatura.valorTotalFatura())) {
			// Obtem a diferença e insere no fundo
			Float dieferenca = NumeroUtil.diminuirDinheiro(
					fatura.valorTotalFatura(), valorSomaParticipacao, 3);
			dieferenca = NumeroUtil.deixarFloatDuasCasas(dieferenca);
			
			if (dieferenca < 2f) {
				Participacao fundo = mapaParticipacao.get(0);
				
				Float valorNovo = NumeroUtil.somarDinheiro(
						fundo.getValorTotalParticipacao(), dieferenca, 3);
				valorNovo = NumeroUtil.deixarFloatDuasCasas(valorNovo);
				
				fundo.setValorTotalParticipacao(valorNovo);
				
				em.persist(fundo);
			} else {
				System.out.println("Diferença é maior que 2: " + dieferenca);
			}
			

		}

	}
	
	/**
	 * Zera todas as participacoes de trabalho para uma fatura
	 * @param idFatura
	 * @param p
	 */
	public void exlcuirCriterioTrabalhoPorFatura(Integer idFatura, Prolabore p) {
		// TODO Auto-generated method stub
		
		List<Participacao> listaParticipacao = listarParticipacaoPorFatura(idFatura);
		
		for (Participacao participacao : listaParticipacao) {
			if(participacao.getPorcentagemParticipacaoTrabalho() > 0) {
				
				if (participacao.getPorcentagemIndicacao() > 0 || participacao.getPorcentagemParticipacaoSocio() > 0 ) {
					participacao.setValorTotalParticipacao(NumeroUtil.diminuirDinheiro(participacao.getValorTotalParticipacao(), participacao.getPorcentagemParticipacaoTrabalho(), 4));
					participacao.setPorcentagemParticipacaoTrabalho(0f);
					em.merge(participacao);
				}
				else {
					em.remove(participacao);
				}
			}
		}
		
	}

}
