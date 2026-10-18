package com.studymatch.model;

import java.util.List;

/**
 * Representa o objeto de transferência de dados (DTO) para a submissão de um quiz.
 * Contém a lista de respostas fornecidas pelo estudante.
 */
public class QuizSubmissionRequest {

    // Lista de respostas associadas à submissão do quiz.
    private List<AnswerDto> answers;

    /**
     * Obtém a lista de respostas da submissão.
     *
     * @return Lista de objetos AnswerDto contendo as respostas.
     */
    public List<AnswerDto> getAnswers() {
        return answers;
    }

    /**
     * Define a lista de respostas da submissão.
     *
     * @param answers Lista de objetos AnswerDto a definir.
     */
    public void setAnswers(List<AnswerDto> answers) {
        this.answers = answers;
    }

    /**
     * Representa uma resposta individual dentro do pedido de submissão do quiz.
     */
    public static class AnswerDto {

        // Identificador único da pergunta a ser respondida.
        private Long questionId;

        // Valor da resposta dada pelo estudante (deve estar compreendido entre 1 e 5).
        private Integer value;

        /**
         * Obtém o identificador da pergunta.
         *
         * @return ID da pergunta.
         */
        public Long getQuestionId() {
            return questionId;
        }

        /**
         * Define o identificador da pergunta.
         *
         * @param questionId ID da pergunta a definir.
         */
        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        /**
         * Obtém o valor da resposta.
         *
         * @return Valor inteiro da resposta.
         */
        public Integer getValue() {
            return value;
        }

        /**
         * Define o valor da resposta.
         *
         * @param value Valor inteiro da resposta a definir.
         */
        public void setValue(Integer value) {
            this.value = value;
        }
    }
}