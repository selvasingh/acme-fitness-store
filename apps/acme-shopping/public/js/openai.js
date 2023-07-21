const CONTEXT_MESSAGE_COUNT = 5;
const API_URL = '/chat/completions';
const API_HEADER = {
  "Content-Type": "application/json"
};
const BUILT_IN_QUESTIONS = [
  "I am looking for an e-bike that can run fast",
  "Show me e-bikes that can run long distance",
  "What's the most popular e-bikes"
];

let MESSAGE_HISTORY = [];

function addMessage(message, senderIsAI) {
  MESSAGE_HISTORY.push({
    content: message,
    role: senderIsAI ? 'assistant' : 'user'
  });

  const sender = senderIsAI ? 'ai' : 'customer';
  const messageElement = $(`<div class="message ${sender}"></div>`);
  const messageSpan = $('<div></div>');

  if (senderIsAI) {
    messageSpan.html(marked.parse(message));
  } else {
    messageSpan.text(message);
  }
  messageElement.append(messageSpan);
  $('#aiChatHistory').append(messageElement);
  $('#aiChatHistory').scrollTop($('#aiChatHistory').prop('scrollHeight'));
}

async function sendMessage() {
  let message = $('#aiChatInputbox').val()?.trim();
  $('#aiChatInputbox').val('');
  if (!message) return;

  const productId = window.location.search.split('?id=')[1] ?? '';

  $('#aiChatInputboxBuiltInQuestions').hide();
  addMessage(message, false);
  $('#aiChatInputboxTyping').show();

  if (MESSAGE_HISTORY.length > CONTEXT_MESSAGE_COUNT) {
    MESSAGE_HISTORY = MESSAGE_HISTORY.slice(0 - CONTEXT_MESSAGE_COUNT);
  }

  try {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: API_HEADER ?? {},
      body: JSON.stringify({ messages: MESSAGE_HISTORY, productId })
    });
    const data = await response.json();
  
    addMessage(data.choices[0].message.content, true);
  } finally {
    $('#aiChatInputboxTyping').hide();
  }
}

function generateBuiltInQuestions() {
  const questionsElement = $('#aiChatInputboxBuiltInQuestions');
  questionsElement.find('.question').remove();

  const questions = [];
  while (questions.length < 3 && questions.length < BUILT_IN_QUESTIONS.length) {
    const randomIndex = Math.floor(Math.random() * BUILT_IN_QUESTIONS.length);
    const randomItem = BUILT_IN_QUESTIONS[randomIndex];
    if (!questions.includes(randomItem)) {
      questions.push(randomItem);
    }
  }

  questions.forEach(question => {
    const questionElement = $(`<button class="question">${question}</button>`);
    questionElement.click(() => {
      $('#aiChatInputbox').val(question);
      sendMessage();
    });
    questionsElement.prepend(questionElement);
  });
}

$(document).ready(function () {
  $('#aiChatInputboxSendButton').click(sendMessage);
  $('#aiChatInputbox').keypress(function (e) {
    if (e.which == 13) {
      sendMessage();
      e.preventDefault();
      return false;
    }
  });
  
  generateBuiltInQuestions();
  $('#aiChatInputboxBuiltInQuestionsRefresh').click(generateBuiltInQuestions);
});
