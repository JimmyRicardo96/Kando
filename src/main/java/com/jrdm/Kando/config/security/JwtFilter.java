package com.jrdm.Kando.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter{
    //El filtro que intercepta cada request

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            // 1. UN SOLO PARSEO: Extraemos todo de una vez
            Claims claims = jwtService.extractAllClaims(token);
            String userId = claims.getSubject();
            List<String> roles = (List<String>) claims.get("roles", List.class);

            // 2. CONSTRUCCIÓN DE AUTHORITIES: Usamos los roles ya extraídos
            var authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // 3. AUTENTICACIÓN: Usamos el userId ya extraído
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException e) {
            // 4. SEGURIDAD: Si el token falla, limpiamos el contexto por si acaso
            SecurityContextHolder.clearContext();
        }

        // 5. CONTINUAR: Siempre seguimos la cadena (si no hay auth, el SecurityConfig decidirá si rebota)
        filterChain.doFilter(request, response);
    }
}
