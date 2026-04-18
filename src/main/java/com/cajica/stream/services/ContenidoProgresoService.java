package com.cajica.stream.services;

import com.cajica.stream.entities.ContenidoProgreso;
import com.cajica.stream.repositories.ContenidoProgresoRepository;
import java.util.Collections;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContenidoProgresoService {

  private static final String TIPO_PDF = "PDF";
  private static final String TIPO_QUIZ = "QUIZ";

  private final ContenidoProgresoRepository repository;

  @Autowired
  public ContenidoProgresoService(ContenidoProgresoRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void marcarPdfCompletado(Long usuarioId, Long cursoId, Long pdfId) {
    marcarCompletado(usuarioId, cursoId, TIPO_PDF, pdfId);
  }

  @Transactional
  public void marcarQuizCompletado(Long usuarioId, Long cursoId, Long quizId) {
    marcarCompletado(usuarioId, cursoId, TIPO_QUIZ, quizId);
  }

  public Set<Long> getPdfsCompletadosIds(Long usuarioId, Long cursoId) {
    if (usuarioId == null || cursoId == null) return Collections.emptySet();
    return repository.findContenidoIdsByUsuarioIdAndCursoIdAndTipo(usuarioId, cursoId, TIPO_PDF);
  }

  public Set<Long> getQuizzesCompletadosIds(Long usuarioId, Long cursoId) {
    if (usuarioId == null || cursoId == null) return Collections.emptySet();
    return repository.findContenidoIdsByUsuarioIdAndCursoIdAndTipo(usuarioId, cursoId, TIPO_QUIZ);
  }

  private void marcarCompletado(Long usuarioId, Long cursoId, String tipo, Long contenidoId) {
    if (repository.existsByUsuarioIdAndTipoAndContenidoId(usuarioId, tipo, contenidoId)) return;
    repository.save(new ContenidoProgreso(usuarioId, cursoId, tipo, contenidoId));
  }
}
