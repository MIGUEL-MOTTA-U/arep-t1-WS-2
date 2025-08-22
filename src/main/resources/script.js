function reveal() {
    const reveals = document.querySelectorAll('.reveal');

    reveals.forEach(element => {
        const windowHeight = window.innerHeight;
        const elementTop = element.getBoundingClientRect().top;
        const elementVisible = 150;

        if (elementTop < windowHeight - elementVisible) {
            element.classList.add('active');
        }
    });
}

window.addEventListener('scroll', reveal);

document.querySelectorAll('.floating-element').forEach(element => {
    element.addEventListener('click', function() {
        this.style.animation = 'none';
        setTimeout(() => {
            this.style.animation = 'rotate 2s linear infinite';
        }, 100);

        // Crear efecto de ondas
        const ripple = document.createElement('div');
        ripple.style.cssText = `
                    position: absolute;
                    border-radius: 50%;
                    background: rgba(64, 224, 208, 0.6);
                    transform: scale(0);
                    animation: ripple 0.6s linear;
                    pointer-events: none;
                `;

        const rect = this.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        ripple.style.width = ripple.style.height = size + 'px';
        ripple.style.left = (rect.left + rect.width / 2 - size / 2) + 'px';
        ripple.style.top = (rect.top + rect.height / 2 - size / 2) + 'px';

        document.body.appendChild(ripple);

        setTimeout(() => {
            document.body.removeChild(ripple);
        }, 600);
    });
});

const style = document.createElement('style');
style.textContent = `
            @keyframes ripple {
                to {
                    transform: scale(4);
                    opacity: 0;
                }
            }
        `;
document.head.appendChild(style);

window.addEventListener('scroll', () => {
    const scrolled = window.pageYOffset;
    const parallaxElements = document.querySelectorAll('.particle');

    parallaxElements.forEach((element, index) => {
        const speed = (index + 1) * 0.1;
        element.style.transform = `translateY(${scrolled * speed}px)`;
    });
});

reveal();