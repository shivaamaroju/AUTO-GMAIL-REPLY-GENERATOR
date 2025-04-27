function addAIReplyButton() {
    const replyToolbar = findReplyToolbar();

    if (!replyToolbar) {
        return; 
    }

    if (document.getElementById('ai-reply-btn')) {
        return;
    }

    const button = document.createElement("button");
    button.innerText = "AI Reply";
    button.id = "ai-reply-btn";
    button.style.marginLeft = "8px";
    button.style.padding = "6px 12px";
    button.style.cursor = "pointer";
    button.style.background = "#1a73e8";
    button.style.color = "white";
    button.style.border = "none";
    button.style.borderRadius = "4px";

    button.onclick = async () => {
        const emailBody = extractEmailBody();
        const aiReply = await fetchAIReply(emailBody);
        insertAIReply(aiReply);
    };

    replyToolbar.appendChild(button);
}

function findReplyToolbar() {
  
    const sendButtons = document.querySelectorAll('div[role="button"][aria-label*="Send"]');

    for (const sendButton of sendButtons) {
        const toolbar = sendButton.parentElement;  
        if (toolbar) {
            return toolbar;
        }
    }
    return null;
}

function extractEmailBody() {
    const emailSections = document.querySelectorAll(".ii.gt"); 
    let emailText = "";
    emailSections.forEach(section => {
        emailText += section.innerText + "\\n\\n";
    });
    return emailText.trim();
}

async function fetchAIReply(emailBody) {
    try {
        const response = await fetch("http://localhost:8081/api/email/generate", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                emailContent: emailBody,
                tone: "polite"
            })
        });

        if (!response.ok) {
            throw new Error(`Failed to fetch AI reply: ${response.status}`);
        }

        const replyText = await response.text();
        return replyText;
    } catch (error) {
        console.error("Error fetching AI reply:", error);
        return "Error generating AI reply.";
    }
}

function insertAIReply(reply) {
   
    const replyBox = document.querySelector('div[aria-label="Message Body"]');

    if (replyBox) {
        replyBox.innerText = reply;  
    } else {
        alert("Could not find reply box.");
    }
}

setInterval(addAIReplyButton, 2000);
