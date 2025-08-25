// Efectos sutiles de interacción
document.addEventListener('mousemove', (e) => {
    const particles = document.querySelectorAll('.particle');

    particles.forEach((particle, index) => {
        const speed = (index + 1) * 0.00005;
        const x = (e.clientX * speed);
        const y = (e.clientY * speed);

        particle.style.transform += ` translate(${x}px, ${y}px)`;
    });
});

// Efecto de parallax muy sutil en el contenido
window.addEventListener('scroll', () => {
    const scrolled = window.pageYOffset;
    const content = document.querySelector('.content');
    const parallax = scrolled * 0.05;

    content.style.transform = `translateY(${parallax}px)`;
});

// Animación de typing para el subtítulo (opcional)
function typeWriter() {
    const subtitle = document.querySelector('.subtitle');
    const text = subtitle.textContent;
    subtitle.textContent = '';

    let i = 0;
    const timer = setInterval(() => {
        if (i < text.length) {
            subtitle.textContent += text.charAt(i);
            i++;
        } else {
            clearInterval(timer);
        }
    }, 100);
}

// Activar después de la animación inicial
setTimeout(typeWriter, 1500);

// Efecto de respiración sutil en el contenido
setInterval(() => {
    const content = document.querySelector('.content');
    content.style.transform = 'scale(1.002)';

    setTimeout(() => {
        content.style.transform = 'scale(1)';
    }, 1000);
}, 8000);