document.getElementById('openGmail').onclick = () => {
    chrome.tabs.create({ url: 'https://mail.google.com/' });
};
